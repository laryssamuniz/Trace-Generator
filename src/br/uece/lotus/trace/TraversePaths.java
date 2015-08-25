package br.uece.lotus.trace;

import br.uece.lotus.State;
import br.uece.lotus.Transition;
import java.util.ArrayList;
import java.util.Random;

public class TraversePaths{

    private static class PathStruct{

        public ArrayList<Transition> mPath = new ArrayList<>();
        public ArrayList<ArrayList<Transition>> mCyclesPassed = new ArrayList<>();
        public ArrayList<State> mVisitedStates = new ArrayList<>();
        public int mTransitionsVisited = 0;
    }

    public static ArrayList<Transition> IdentifyLastCycle(ArrayList<Transition> path){
        
        ArrayList<Transition> lastCycle = new ArrayList<>();

        State cycleNode = path.get(path.size() - 1).getDestiny();

        for (int i = path.size() - 1; i >= 0; --i){
            
            Transition t = path.get(i);
            lastCycle.add(0, t);

            if (t.getSource() == cycleNode){
                return lastCycle;
            }
        }

        return null;
    }

    private static boolean hasSomeNewPlaceToGo(ArrayList<Transition> path){
        
        if (path.size() == 0){
            return false;
        }

        ArrayList<Transition> list = new ArrayList<>();
        list.add(path.get(path.size() - 1));
        ArrayList<Transition> visited = new ArrayList<>();

        while (!list.isEmpty()){
            
            Transition currentTransition = list.get(0);
            State currentNode = list.get(0).getDestiny();
            list.remove(0);
            visited.add(currentTransition);

            for (Transition t : currentNode.getOutgoingTransitions()){
                
                if (!path.contains(t) && t.getProbability() > 0.0){
                    return true;
                }

                if (!visited.contains(t)){
                    list.add(t);
                }
            }
        }

        return false;
    }

    private static boolean isSelfLoopState(State node){
        
        if (node.getOutgoingTransitionsCount() == 1){
            
            State destiny = ((ArrayList<Transition>) node.getOutgoingTransitions()).get(0).getDestiny();

            if (destiny == node){
                return true;
            }
        }

        return false;
    }

    private static boolean isHaltState(ArrayList<Transition> graph, State currentNode, int visitedTransitions){
        
        if (currentNode.isFinal()
                || currentNode.isError()
                || currentNode.getOutgoingTransitionsCount() == 0
                || visitedTransitions == graph.size() || isSelfLoopState(currentNode)){
            return true;
        }

        return false;
    }

    private static Transition selectProbTransition(State node){
        
        ArrayList<Transition> list = (ArrayList<Transition>) node.getOutgoingTransitions();
        Double sumProbabilities = 0.0;
        Double w = 0.0;
        Transition ret = null;
        Random randomGen = new Random();

        //System.out.println("LIST SIZE: " + list.size());

        for (Transition t : node.getOutgoingTransitions()){
            sumProbabilities += t.getProbability();
        }

        w = sumProbabilities * (randomGen.nextInt(101) % 100 + 1);

        while (w > 0){
            ret = list.get((randomGen.nextInt(list.size())));
            w -= ret.getProbability();
        }

        //System.out.println("Selected Transition: " + ret.getLabel());

        return ret;
    }

    public static ArrayList<Transition> generatePath(ArrayList<Transition> graph, State root){
        
        PathStruct pathStruct = new PathStruct();
        ArrayList<Transition> transitionList = new ArrayList<>();

        Transition init = selectProbTransition(root);

        if (init != null){
            transitionList.add(init);
        }
        pathStruct.mVisitedStates.add(init.getSource());

        while (!transitionList.isEmpty()){
            
            Transition currentTransition = transitionList.get(0);
            State currentNode = currentTransition.getDestiny();
            pathStruct.mPath.add(currentTransition);
            transitionList.remove(0);

            if (isHaltState(graph, currentNode, pathStruct.mTransitionsVisited)){
                break;
            }

            if (!pathStruct.mVisitedStates.contains(currentNode)){
                pathStruct.mVisitedStates.add(currentNode);
            } else{ 
                
                // Returning to some already visited state -> CYCLE
                // Get last cycle
                
                ArrayList<Transition> lastCycle = IdentifyLastCycle(pathStruct.mPath);

                if (!pathStruct.mCyclesPassed.contains(lastCycle)){
                    // This is the first time that lastCycle has appeared
                    pathStruct.mCyclesPassed.add(lastCycle);

                    // Verify if the path can go to some unvisited transition from the current state
                    if (!hasSomeNewPlaceToGo(pathStruct.mPath)){
                        break;
                    }
                } else{ 
                    // Verify if currentPath already has passed for that cycle
                    if (!pathStruct.mCyclesPassed.contains(lastCycle)){
                        pathStruct.mCyclesPassed.add(lastCycle);
                    }
                }
            }

            transitionList.add(selectProbTransition(currentNode));
        }

        return pathStruct.mPath;
    }
}
