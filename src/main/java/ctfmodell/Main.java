package ctfmodell;

import ctfmodell.container.SimulationContainer;
import ctfmodell.controller.Controller;
import ctfmodell.database.DatabaseManager;
import ctfmodell.model.Landscape;
import ctfmodell.provider.PropertyProvider;
import ctfmodell.tutor.Tutor;
import ctfmodell.tutor.TutorialSystem;
import ctfmodell.tutor.TutorialSystemImpl;
import ctfmodell.util.Helper;
import ctfmodell.view.LandscapePanel;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

@SuppressWarnings({"ConstantConditions", "Duplicates"})
public class Main extends Application {

    public static final String PROGAM_FOLDER = "programs";
    public static final String LANDSCAPE_FOLDER = "landscapes";
    public static final String XML_FOLDER = "xml";
    public static final String PREFIX_1 = "public class ";
    public static final String PREFIX_2 = " extends ctfmodell.model.PoliceOfficer {\n\npublic ";
    public static final String POSTFIX = "\n\n}";
    public static SimulationContainer simulations = new SimulationContainer();
    private static final PropertyProvider propertyProvider = new PropertyProvider();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        DatabaseManager.createDatabase();
        createProgramFolder();
        createSerializationFolder();
        createXMLFolder();
        String defaultCode = loadDefaultOfficerCode();
        createAndStartSimulation(primaryStage, "DefaultOfficer", defaultCode);
    }

    private static String loadDefaultOfficerCode() {
        Path directory = Paths.get(PROGAM_FOLDER, "DefaultOfficer.java");

        if (Files.exists(directory)) {
            try {
                String prefix = PREFIX_1 + "DefaultOfficer" + PREFIX_2;
                String code = new String(Files.readAllBytes(directory));
                if (code.length() < 1) return null;
                code = code.replace(prefix, "");
                code = code.substring(0, code.length() - 1);
                return code;
            } catch (IOException e) {
                System.err.println(e.getMessage());
                return null;
            }
        } else {
            return null;
        }
    }

    private static void createProgramFolder() {
        Path directory = Paths.get(PROGAM_FOLDER);

        if (!Files.exists(directory)) {
            try {
                Files.createDirectory(directory);
            } catch (IOException e) {
                System.err.println(e.getMessage());
            } finally {
                createDefaultOfficer();
            }
        } else {
            System.err.println("Programm-Verzeichnis existiert schon!");
            createDefaultOfficer();
        }

    }

    private static void createSerializationFolder() {
        Path directory = Paths.get(LANDSCAPE_FOLDER);

        if (!Files.exists(directory)) {
            try {
                Files.createDirectory(directory);
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        } else {
            System.err.println("LANDSCAPE_FOLDER-Verzeichnis existiert schon!");
        }

    }

    private static void createXMLFolder() {
        Path directory = Paths.get(XML_FOLDER);

        if (!Files.exists(directory)) {
            try {
                Files.createDirectory(directory);
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        } else {
            System.err.println("XML-Verzeichnis existiert schon!");
        }

    }

    public static void createAndStartSimulation(Stage primaryStage, String editorClass, String code) throws IOException {
        if (!Helper.isValidClassName(editorClass)) {
            System.err.println(editorClass + "ist kein valider Klassenname!");
            System.exit(0);
        }

        if (code == null) {
            code = "void main() {\n\n}";
        }


        establishRMIConnection(propertyProvider.getRole());

        FXMLLoader loader = new FXMLLoader(Main.class.getClassLoader().getResource("main.fxml"));
        Parent root = loader.load();

        //Add Observers
        Landscape landscape = new Landscape();
        LandscapePanel landscapePanel = new LandscapePanel(landscape);
        landscape.addObserver(landscapePanel);


        Controller controller = loader.getController();
        controller.setEditorClass(editorClass);
        controller.initialize(landscape, landscapePanel, code, propertyProvider);
        controller.initializeEventHandler();
        controller.setOfficerLabel(editorClass);


        //Add LandscapePanel to GUI
        SplitPane splitPane = (SplitPane) ((BorderPane) root).getCenter();
        VBox vBox = (VBox) splitPane.getItems().get(1);
        HBox hBox = (HBox) vBox.getChildren().get(0);

        ScrollPane scrollPane = (ScrollPane) hBox.getChildren().get(0);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setContent(landscapePanel);

        primaryStage.setTitle("Capture The Flag Simulation");
        primaryStage.getIcons().add(new Image(Main.class.getClassLoader().getResourceAsStream("image/menu/police_with_flag.png")));
        primaryStage.setScene(new Scene(root));
        simulations.addSimulation(editorClass);

        primaryStage.setOnCloseRequest((event) -> {
            controller.saveCode();
            simulations.removeSimulation(editorClass);
        });

        controller.setStage(primaryStage);
        controller.saveCode();

        primaryStage.show();
    }

    public static void establishRMIConnection(String role) {
        if (simulations.getContainerSize() > 0) {
            return;
        }

        int port = Integer.valueOf(propertyProvider.getPort());

        try {
            if ("tutor".equals(role)) {
                Tutor.tutorialSystem = new TutorialSystemImpl();
                LocateRegistry.createRegistry(port);
                Tutor.registry = LocateRegistry.getRegistry(port);
                Tutor.registry.rebind("TutorialSystem", Tutor.tutorialSystem);
                System.out.println("TutorialSystem angemeldet");
            } else {
                Tutor.registry = LocateRegistry.getRegistry(port);
                Tutor.tutorialSystem = (TutorialSystem) Tutor.registry.lookup("TutorialSystem");
            }
        } catch (RemoteException | NotBoundException e) {
            System.err.println("Socket-Verbindung ist fehlgeschlagen");
        }
    }

    @Override
    public void stop() {
        try {
            UnicastRemoteObject.unexportObject(Tutor.tutorialSystem, true);
            DatabaseManager.shutdownDatabase();
        } catch (NoSuchObjectException e) {
            // Tritt auf, wenn man Student ist
        }
    }

    private static void createDefaultOfficer() {
        Path directory = Paths.get(PROGAM_FOLDER, "DefaultOfficer.java");

        if (!Files.exists(directory)) {
            try {
                Files.createFile(directory);
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        } else {
            System.err.println("DefaultOfficer.java gibt es schon existiert schon!");
        }
    }

}
