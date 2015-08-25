package br.uece.lotus.trace;

import br.uece.lotus.Component;
import br.uece.lotus.Transition;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javax.swing.JOptionPane;

public class TraceProbabilityWindow extends TraceGeneratorWindow{

    private TextField mTextTrace;
    private volatile boolean mGeneratingCSV = false;
    private boolean mLastGenerationSuccess = false;
    private Label mGeneratingStatusLabel;
    private Button mSubmitTrace;
    private TableView<StringTableItem> mTableView;
    private TableColumn<StringTableItem, String> mTableColTraces;
    private ObservableList<StringTableItem> mTableContent = null;
    private TextField mTextTime;

    public TraceProbabilityWindow(Component c, boolean autoSetProb){
        super(c, autoSetProb);
        init();
    }
    
    public class StringTableItem{

        public StringTableItem(ArrayList<Transition> list){
            
            value = "";
            
            for (Transition t : list){
                value += t.getLabel() + ", ";
            }
            
            if (list.size()> 0){
                value = value.substring(0, value.length()-1);
            }
            
            transitionList = list;
            
            if (list == null){
                transitionList = new ArrayList<>();
            }
        }
        
        public ArrayList<Transition> transitionList;
        private String value;
        public String getValue(){
            return value;
        }
    }

    @Override
    public void start(){

    }

    private void init(){

        Label mLabel = new Label("Traces:");
        Label mTime = new Label("Generation Time:");

        mTextTrace = new TextField();
        mTextTime = new TextField();

        mSubmitTrace = new Button("Submit");

        mGeneratingStatusLabel = new Label();

        mTableView = new TableView();
        mTableView.setPrefHeight(200);

        mTableView.setOnMouseClicked(onTableClick);
        
        mTableColTraces = new TableColumn<>("Traces");
        mTableColTraces.setPrefWidth(400);
        mTableColTraces.setCellValueFactory(new PropertyValueFactory<>("value"));
        mTableView.getColumns().addAll(mTableColTraces);
        HBox hbox = new HBox(8);
        hbox.getChildren().addAll(mLabel, mTextTrace, mTime, mTextTime, mSubmitTrace, mGeneratingStatusLabel);

        AnchorPane.setTopAnchor(hbox, 25D);
        AnchorPane.setLeftAnchor(hbox, 5D);
        AnchorPane.setRightAnchor(hbox, 0D);

        AnchorPane.setLeftAnchor(mTableView, 0D);
        AnchorPane.setRightAnchor(mTableView, 0D);
        AnchorPane.setBottomAnchor(mTableView, 20D);
        getChildren().add(hbox);
        getChildren().add(mTableView);

        mSubmitTrace.setOnAction(new EventHandler<ActionEvent>(){

            @Override
            public void handle(ActionEvent event){
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Save Trace project");
                fileChooser.setInitialDirectory(
                        new File(System.getProperty("user.home"))
                );

                fileChooser.getExtensionFilters().addAll(
                        new FileChooser.ExtensionFilter("Trace Files (*.csv)", "*.csv"),
                        new FileChooser.ExtensionFilter("Trace Files (*.txt)", "*.txt"),
                        new FileChooser.ExtensionFilter("All files", "*")
                );

                File file = fileChooser.showSaveDialog(null);

                if (file != null){
                    mGeneratingCSV = true;
                    mLastGenerationSuccess = false;

                    Thread genThread = new Thread(new Runnable(){

                        @Override
                        public void run(){
                            try{
                                mTableContent = GenerateCSV(file);
                                mLastGenerationSuccess = true;
                            } catch (IOException ex){
                                Logger.getLogger(TraceProbabilityWindow.class.getName()).log(Level.SEVERE, null, ex);
                            } finally{
                                Platform.runLater(new Runnable(){

                                    @Override
                                    public void run(){
                                        AdjustWindowSettings(false);
                                        mTableView.setItems(mTableContent);

                                        if (mLastGenerationSuccess == true){
                                            JOptionPane.showMessageDialog(null, "File Successfully Saved");
                                        } else{
                                            JOptionPane.showMessageDialog(null, "File was not Saved succesfully");
                                        }
                                    }
                                });
                                mGeneratingCSV = false;
                            }
                        }
                    });

                    AdjustWindowSettings(true);
                    genThread.start();

                }
            }

        });

    }
    
    protected EventHandler<? super MouseEvent> onTableClick = new EventHandler<MouseEvent>(){

        @Override
        public void handle(MouseEvent e){
            
            StringTableItem selectedPath = mTableView.getSelectionModel().getSelectedItem();

            if (selectedPath == null){
                return;
            }

            applyDisableAll();

            for (Transition t : selectedPath.transitionList){
                applyEnableStyle(t);
                applyEnableStyle(t.getDestiny());
                applyEnableStyle(t.getSource());
            }
        }
    };

    private void AdjustWindowSettings(boolean generating){
        
        if (generating){
            mTextTrace.setDisable(true);
            mSubmitTrace.setDisable(true);
            mTextTrace.setEditable(false);
            mTextTime.setDisable(true);
            mTextTime.setEditable(false);
            mGeneratingStatusLabel.setText("Generating Traces...");
            mGeneratingStatusLabel.setTextFill(Color.web("#0076a3"));
            mGeneratingStatusLabel.setFont(Font.font(null, FontWeight.BOLD, 12));
        } else{
            mTextTrace.setDisable(false);
            mSubmitTrace.setDisable(false);
            mTextTrace.setEditable(true);
            mTextTime.setDisable(false);
            mTextTime.setEditable(true);
            mGeneratingStatusLabel.setText("");
        }
    }

    private ObservableList<StringTableItem> GenerateCSV(File file) throws IOException{

        ObservableList<StringTableItem> obs = FXCollections.observableArrayList();

        try (PrintStream out = new PrintStream(file)){

            Integer num = Integer.parseInt(mTextTrace.getText());
            Integer time = 0;

            if (!mTextTime.getText().equals("")){
                time = Integer.parseInt(mTextTime.getText());
            }
            
            for (int i = 0; i <num; ++i){

                ArrayList<Transition> path = TraversePaths.generatePath((ArrayList<Transition>) mViewer.getComponent().getTransitions(), mViewer.getComponent().getInitialState());

                Transition t;
                String str = "";
                
                for (int x = 0; x <path.size(); ++x){
                    t = path.get(x);
                    str += t.getLabel();

                    if (x <path.size() - 1){
                        str += ", ";
                    }
                }
                
                if (path.size()> 0){
                    out.println(str);
                    obs.add(new StringTableItem(path));
                }

                try{
                    Platform.runLater(new Runnable(){
                        @Override
                        public void run(){

                            applyDisableAll();

                            for (Transition t : path){
                                applyEnableStyle(t);
                                applyEnableStyle(t.getDestiny());
                                applyEnableStyle(t.getSource());
                            }
                        }
                    });

                    Thread.sleep(time);

                } catch (InterruptedException ex){
                    Logger.getLogger(TraceProbabilityWindow.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
            out.println();
        }        
        return obs;
    }

}
