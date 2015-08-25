package br.uece.lotus.trace;

import br.uece.lotus.Component;
import br.uece.lotus.Transition;
import br.uece.lotus.project.ProjectExplorer;
import br.uece.seed.app.UserInterface;
import br.uece.seed.ext.ExtensionManager;
import br.uece.seed.ext.Plugin;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

public class TraceGeneratorPlugin extends Plugin implements TraceGenerator {

    private ProjectExplorer mProjectExplorer;

    private UserInterface mUserInterface;

    private boolean autoSetProb = false;

    @Override
    public void show(Component c, boolean editable) {

        TraceGeneratorWindow w;

        w = new TraceProbabilityWindow(c, autoSetProb);

        w.start();

        int id = mUserInterface.getCenterPanel().newTab(c.getName() + " - [TraceGenerator]", w, true);
        mUserInterface.getCenterPanel().showTab(id);
    }

    private Runnable TraceGenerator = () -> {

        Component c = mProjectExplorer.getSelectedComponent();

        try {
            if (c == null) {
                JOptionPane.showMessageDialog(null, "Select a component!");
                return;
            }

            for (Transition t : c.getTransitions()) {
                autoSetProb = false;
                if (t.getProbability() == null) {
                    autoSetProb = true;
                }
            }

            show(c.clone(), true);

        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(TraceGenerator.class.getName()).log(Level.SEVERE, null, ex);

        }

    };

//        private Runnable TraceGeneratorAutoSetProb = () -> {
//
//        Component c = mProjectExplorer.getSelectedComponent();
//
//        try {
//            if (c == null) {
//                JOptionPane.showMessageDialog(null, "Select a component!");
//                return;
//            }
//            
//            autoSetProb = true;
//            show(c.clone(), true);
//
//        } catch (CloneNotSupportedException ex) {
//            Logger.getLogger(TraceGenerator.class.getName()).log(Level.SEVERE, null, ex);
//
//        }
//
//    };
    @Override
    public void onStart(ExtensionManager extensionManager) throws Exception {

        mUserInterface = extensionManager.get(UserInterface.class);
        mProjectExplorer = extensionManager.get(ProjectExplorer.class);

        mUserInterface.getMainMenu().addItem(Integer.MAX_VALUE - 1, "Trace Generator/Generate Traces", TraceGenerator);
        //mUserInterface.getMainMenu().addItem(Integer.MAX_VALUE-1, "Trace Generator/Random Probabilistic", TraceGeneratorAutoSetProb);

    }

}
