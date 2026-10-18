package org.telegram.core.presenter;

import org.telegram.core.data.repository.PollRepository;
import org.telegram.core.data.repository.PollRepositoryImpl;
import org.telegram.ui.PollCreateActivity;

public class PollCreatePresenter {

    public interface PollCreateView {
        void updateDoneButton(boolean enabled, boolean allowClick);
        void onPollCreated(PollRepository.PollData pollData);
    }

    private PollCreateView view;
    private final PollRepository repository;

    public PollCreatePresenter() {
        this(new PollRepositoryImpl());
    }

    public PollCreatePresenter(PollRepository repository) {
        this.repository = repository;
    }

    public void attachView(PollCreateView view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }

    public void validatePoll(boolean quizPoll, boolean[] answersChecks, String[] answers, String questionString, CharSequence solutionString) {
        int checksCount = 0;
        if (quizPoll && answersChecks != null) {
            for (int a = 0; a < answersChecks.length; a++) {
                if (answers[a] != null && !answers[a].trim().isEmpty() && answersChecks[a]) {
                    checksCount++;
                }
            }
        }
        boolean enabled = repository.isPollValid(quizPoll, answersChecks, answers, questionString, solutionString);
        boolean allowClick = quizPoll && checksCount == 0 || enabled;
        if (view != null) {
            view.updateDoneButton(enabled, allowClick);
        }
    }

    public void buildAndSendPoll(boolean multipleChoise, boolean quizPoll, boolean anonymousPoll, String questionString, String[] answers, boolean[] answersChecks, CharSequence solutionString, int currentAccount, PollCreateActivity.PollCreateActivityDelegate delegate, boolean notify, int scheduleDate) {
        PollRepository.PollData pollData = repository.createPoll(multipleChoise, quizPoll, anonymousPoll, questionString, answers, answersChecks, solutionString, currentAccount);
        if (delegate != null) {
            delegate.sendPoll(pollData.poll, pollData.params, notify, scheduleDate);
        }
        if (view != null) {
            view.onPollCreated(pollData);
        }
    }
}
