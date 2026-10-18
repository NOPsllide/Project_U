package org.telegram.core.data.repository;

import android.text.TextUtils;

import org.telegram.messenger.MediaDataController;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.SerializedData;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.Components.ChatAttachAlertPollLayout;

import java.util.ArrayList;
import java.util.HashMap;

public class PollRepositoryImpl implements PollRepository {

    @Override
    public boolean isPollValid(boolean quizPoll, boolean[] answersChecks, String[] answers, String questionString, CharSequence solutionString) {
        int checksCount = 0;
        if (quizPoll && answersChecks != null) {
            for (int a = 0; a < answersChecks.length; a++) {
                if (!TextUtils.isEmpty(ChatAttachAlertPollLayout.getFixedString(answers[a])) && answersChecks[a]) {
                    checksCount++;
                }
            }
        }
        if (!TextUtils.isEmpty(ChatAttachAlertPollLayout.getFixedString(solutionString)) && solutionString.length() > ChatAttachAlertPollLayout.MAX_SOLUTION_LENGTH) {
            return false;
        } else if (TextUtils.isEmpty(ChatAttachAlertPollLayout.getFixedString(questionString)) || questionString.length() > ChatAttachAlertPollLayout.MAX_QUESTION_LENGTH) {
            return false;
        } else {
            int count = 0;
            if (answers != null) {
                for (int a = 0; a < answers.length; a++) {
                    if (!TextUtils.isEmpty(ChatAttachAlertPollLayout.getFixedString(answers[a]))) {
                        if (answers[a].length() > ChatAttachAlertPollLayout.MAX_ANSWER_LENGTH) {
                            count = 0;
                            break;
                        }
                        count++;
                    }
                }
            }
            if (count < 2 || quizPoll && checksCount < 1) {
                return false;
            }
        }
        return true;
    }

    @Override
    public PollData createPoll(boolean multipleChoise, boolean quizPoll, boolean anonymousPoll, String questionString, String[] answers, boolean[] answersChecks, CharSequence solutionString, int currentAccount) {
        TLRPC.TL_messageMediaPoll poll = new TLRPC.TL_messageMediaPoll();
        poll.poll = new TLRPC.TL_poll();
        poll.poll.multiple_choice = multipleChoise;
        poll.poll.quiz = quizPoll;
        poll.poll.public_voters = !anonymousPoll;
        poll.poll.question = ChatAttachAlertPollLayout.getFixedString(questionString).toString();
        SerializedData serializedData = new SerializedData(10);
        for (int a = 0; a < answers.length; a++) {
            if (TextUtils.isEmpty(ChatAttachAlertPollLayout.getFixedString(answers[a]))) {
                continue;
            }
            TLRPC.TL_pollAnswer answer = new TLRPC.TL_pollAnswer();
            answer.text = ChatAttachAlertPollLayout.getFixedString(answers[a]).toString();
            answer.option = new byte[1];
            answer.option[0] = (byte) (48 + poll.poll.answers.size());
            poll.poll.answers.add(answer);
            if ((multipleChoise || quizPoll) && answersChecks[a]) {
                serializedData.writeByte(answer.option[0]);
            }
        }
        HashMap<String, String> params = new HashMap<>();
        params.put("answers", Utilities.bytesToHex(serializedData.toByteArray()));
        poll.results = new TLRPC.TL_pollResults();
        CharSequence solution = ChatAttachAlertPollLayout.getFixedString(solutionString);
        if (solution != null) {
            poll.results.solution = solution.toString();
            CharSequence[] message = new CharSequence[]{solution};
            ArrayList<TLRPC.MessageEntity> entities = MediaDataController.getInstance(currentAccount).getEntities(message, true);
            if (entities != null && !entities.isEmpty()) {
                poll.results.solution_entities = entities;
            }
            if (!TextUtils.isEmpty(poll.results.solution)) {
                poll.results.flags |= 16;
            }
        }
        return new PollData(poll, params);
    }
}
