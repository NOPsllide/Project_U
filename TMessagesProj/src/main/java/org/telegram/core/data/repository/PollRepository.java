package org.telegram.core.data.repository;

import org.telegram.tgnet.TLRPC;
import java.util.HashMap;

public interface PollRepository {

    class PollData {
        public TLRPC.TL_messageMediaPoll poll;
        public HashMap<String, String> params;

        public PollData(TLRPC.TL_messageMediaPoll poll, HashMap<String, String> params) {
            this.poll = poll;
            this.params = params;
        }
    }

    boolean isPollValid(boolean quizPoll, boolean[] answersChecks, String[] answers, String questionString, CharSequence solutionString);

    PollData createPoll(boolean multipleChoise, boolean quizPoll, boolean anonymousPoll, String questionString, String[] answers, boolean[] answersChecks, CharSequence solutionString, int currentAccount);
}
