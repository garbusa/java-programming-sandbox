package ctfmodell.tutor;

import ctfmodell.model.StudentExample;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Das RMI Remote-Objekt, welches die Requests Queue für den Tutor realisiert
 * Beinhaltet alle ankommenden Requests und bereits beantworteten Studentenanfragen
 * <p>
 * Tutor bezieht Anfragen aus {@link TutorialSystemImpl#requests}
 * Student bezieht Anfragen aus {@link TutorialSystemImpl#finishedRequests}
 *
 * @author Nick Garbusa
 */
public class TutorialSystemImpl extends UnicastRemoteObject implements TutorialSystem {

    private Queue<StudentExample> requests;
    private List<StudentExample> finishedRequests;

    public TutorialSystemImpl() throws RemoteException {
        super();
        this.requests = new ConcurrentLinkedQueue<>();
        this.finishedRequests = new ArrayList<>();
    }

    public void sendRequest(StudentExample example) {
        requests.add(example);
    }

    public StudentExample checkResponse(String id) {
        System.out.println(id);

        StudentExample answer = null;
        for (StudentExample example : this.finishedRequests) {
            if (example.getStudentId().equals(id)) {
                answer = example;
                this.finishedRequests.remove(example);
                break;
            }
        }

        return answer;
    }

    public StudentExample loadNextRequest() {
        return this.requests.poll();
    }

    void saveAnswer(StudentExample example) {
        this.finishedRequests.add(example);
    }

}
