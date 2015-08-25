package br.uece.lotus.trace;

import br.uece.lotus.Component;
import br.uece.lotus.State;
import br.uece.lotus.Transition;
import br.uece.lotus.viewer.ComponentViewImpl;
import java.util.ArrayList;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.AnchorPane;

public abstract class TraceGeneratorWindow extends AnchorPane {

    protected final ToolBar mToolbar;
    protected ScrollPane mScrollPanel;

    protected ComponentViewImpl mViewer;

    private void init() {

        mViewer = new ComponentViewImpl();
        mScrollPanel = new ScrollPane(mViewer);

        AnchorPane.setTopAnchor(mScrollPanel, 70D);
        AnchorPane.setLeftAnchor(mScrollPanel, 0D);
        AnchorPane.setRightAnchor(mScrollPanel, 0D);
        AnchorPane.setBottomAnchor(mScrollPanel, 0D);

        mViewer.minHeightProperty().bind(mScrollPanel.heightProperty());
        mViewer.minWidthProperty().bind(mScrollPanel.widthProperty());

        getChildren().add(mScrollPanel);
    }

    TraceGeneratorWindow(Component c, boolean autoSetProbabilities) {
        
        init();

        mViewer.setComponent(c);

        mToolbar = new ToolBar();

        AnchorPane.setTopAnchor(mToolbar, 0D);
        AnchorPane.setLeftAnchor(mToolbar, 0D);
        AnchorPane.setRightAnchor(mToolbar, 0D);

        getChildren().add(mToolbar);

        if (autoSetProbabilities) {
            setProbabilities();
        }

    }

    private void setProbabilities() {
        
        for (State s : mViewer.getComponent().getStates()) {
            
            int numTransitions = s.getOutgoingTransitionsCount();
            Double probability = 1.0 / numTransitions;

            for (Transition t : s.getOutgoingTransitions()) {
                t.setProbability(probability);
            }
        }
    }

    public abstract void start();

    protected void showChoices(State state) {
        applyEnableStyle(state);
        for (Transition t : state.getOutgoingTransitions()) {
            applyChoiceStyle(t);
            applyChoiceStyle(t.getDestiny());
        }
    }

    protected void applyEnableStyle(State s) {
        s.setColor(null);
        s.setTextColor("black");
        s.setTextSyle(State.TEXTSTYLE_NORMAL);
        s.setBorderColor("black");
        s.setBorderWidth(1);
    }

    protected void applyEnableStyle(Transition t) {
        t.setColor("black");
        t.setTextSyle(Transition.TEXTSTYLE_NORMAL);
        t.setTextColor("black");
        t.setWidth(1);
    }

    protected void applyDisableAll() {
        
        State s = mViewer.getComponent().getInitialState();
        ArrayList<State> stateList = new ArrayList<>();
        ArrayList<Transition> visitedTransitions = new ArrayList<>();
        int i = 0;
        stateList.add(s);

        while (i < stateList.size()) {
            
            s = stateList.get(i);
            applyDisabledStyle(s);
            
            for (Transition t : s.getOutgoingTransitions()) {
                
                if (!stateList.contains(t.getDestiny())) {
                    stateList.add(t.getDestiny());
                }
                if (!visitedTransitions.contains(t)) {
                    applyDisabledStyle(t);
                    visitedTransitions.add(t);
                }
            }
            ++i;
        }
    }

    protected void applyDisabledStyle(State s) {
        s.setColor("#d0d0d0");
        s.setTextColor("#c0c0c0");
        s.setTextSyle(State.TEXTSTYLE_NORMAL);
        s.setBorderColor("gray");
        s.setBorderWidth(1);
    }

    protected void applyDisabledStyle(Transition t) {
        t.setColor("#d0d0d0");
        t.setTextColor("#c0c0c0");
        t.setTextSyle(Transition.TEXTSTYLE_NORMAL);
        t.setWidth(1);
    }

    public void setComponent(Component c) {
        mViewer.setComponent(c);
    }

    private void applyChoiceStyle(Transition t) {
        t.setColor("blue");
        t.setTextSyle(Transition.TEXTSTYLE_BOLD);
        t.setTextColor("blue");
        t.setWidth(2);
    }

    private void applyChoiceStyle(State s) {
        s.setColor(null);
        s.setBorderColor("blue");
        s.setTextSyle(Transition.TEXTSTYLE_BOLD);
        s.setTextColor("blue");
        s.setBorderWidth(2);
    }

    public static class Step {

        private String mAction;
        private String mFrom;
        private String mTo;

        Step(String action, String from, String to) {
            mAction = action;
            mFrom = from;
            mTo = to;
        }

        public String getAction() {
            return mAction;
        }

        public void setAction(String action) {
            this.mAction = action;
        }

        public String getFrom() {
            return mFrom;
        }

        public void setFrom(String from) {
            this.mFrom = from;
        }

        public String getTo() {
            return mTo;
        }

        public void setTo(String to) {
            this.mTo = to;
        }
    }
}
