package gameClient;

import api.*;
import Server.Game_Server_Ex2;
import org.json.JSONObject;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Ex2 implements  Runnable {
    private static Arena _ar;
    private static MyFrame _win;
    private static int level, id;
    private static directed_weighted_graph g;
    private static HashMap<Integer, List<node_data>> path;
    private static HashMap<Integer, CL_Pokemon> target;
    private static game_service game;

    private static boolean firstRun = true;
    private static boolean change;


    /**
     * This is the main method for the driver program.
     * @param args stores the incoming command line arguments for the program.
     */
    public static void main(String[] args) {
        
        if (args.length == 2) {
            try {
                level = Integer.parseInt(args[1]);
                id = Integer.parseInt(args[0]);
            } catch (NumberFormatException e){
                System.out.println("invalid input, try again");
                System.exit(0);
            }
        } else {
            id= MyPanel.PopUpWin.getId();
            level = MyPanel.PopUpWin.getLevel();

        }
        game = Game_Server_Ex2.getServer(level);
        Thread client = new Thread(new Ex2());
        client.start();
    }

    @Override
    public void run() {

        int id = 314805235;
        game.login(id);

        init(game);
        game.startGame();
        _win.setTitle("Ex2 - OOP: (NONE trivial Solution) " + game.toString());
        int ind = 0;


        while (game.isRunning()) {

            moveAgants();
            try {

                long dt =calcDt();
                if (ind % 1 == 0) {
                    _win.repaint();
                }
                _ar.setTime("Time Left: " + (double) game.timeToEnd() / 1000);
                Thread.sleep(dt);
                ind++;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        String res = game.toString();
        System.out.println(res);
        MyPanel.PopUpWinScores s=new MyPanel.PopUpWinScores(game);
        s.paintScores();
        System.exit(0);
    }


    /**
     * at level 23 we calculate the desired dt.
     * @return
     */
    private long calcDt() {


        double de, w;
        double dist = 0;
        long min0 = 0, d;
        long min=minByLevel(min0);

        if(level!=11&&level!=17) {
            for (CL_Agent agent : _ar.getAgents()) {
                int agId = agent.getID();
                if (target.containsKey(agId)) {
                    edge_data agentEdge = agent.get_curr_edge();
                    if (agentEdge != null && agentEdge.getSrc() == target.get(agId).getSrc()) {

                        edge_data e = agent.get_curr_edge();
                        w = e.getWeight();
                        geo_location dest = g.getNode(e.getDest()).getLocation();
                        geo_location src = g.getNode(e.getSrc()).getLocation();
                        de = src.distance(dest);

                        dist = target.get(agId).getLocation().distance(agent.getLocation());

                        double norm = dist / de;
                        double dt = w * norm / agent.getSpeed();
                        d = (long) (1000.0 * dt);
                        if (d < min) {
                            min = d;
                        }
                    }
                }
            }
            if (min == 0) {
                return 30;
            }
        }

        return min;
    }



    private static  long minByLevel(long min){
        if(level==0)
            min=125;
        if(level==1)
            min=125;
        if(level==2)
            min=125;
        if(level==3)
            min=140;
        if(level==4)
            min=125;
        if(level==5)
            min=120;
        if(level==6)
            min=110;
        if(level==7)
            min=115;
        if(level==8)
            min=110;
        if(level==9)
            min=120;
        if(level==10)
            min=110;
        if(level==11)
            min=100;
        if(level==12)
            min=105;
        if(level==13)
            min=110;
        if(level==14)
            min=105;
        if(level==15)
            min=112;
        if(level==16)
            min=115;
        if(level==17)
            min=100;
        if(level==18)
            min=107;
        if(level==19)
            min=109;
        if(level==20)
            min=109;
        if(level==21)
            min=115;
        if(level==22)
            min=110;
        if(level==23)
            min=118;
        return min;
    }
    public void init(game_service game) {
        String graph = game.getGraph();
        dw_graph_algorithms saveG = new DWGraph_Algo();
        saveG.load(save(graph));

        g = saveG.copy();
        _ar = new Arena();
        _ar.setGraph(g);

        String gP = game.getPokemons();
        _ar.setPokemons(Arena.json2Pokemons(gP));

        _win = new MyFrame("test Ex2");
        _win.setSize(1000, 700);
        _win.update(_ar);
        _win.show();


        String info = game.toString();
        JSONObject line;
        try {
            line = new JSONObject(info);
            JSONObject gameInfo = line.getJSONObject("GameServer");
            int agentsNum = gameInfo.getInt("agents");

            System.out.println(info);
            System.out.println(game.getPokemons());

            ArrayList<CL_Pokemon> cl_pok = Arena.json2Pokemons(game.getPokemons());


            for (int a = 0; a < cl_pok.size(); a++) {

                Arena.updateEdge(cl_pok.get(a), g);

            }

            path = new HashMap<>();
            target=new HashMap<>();

            for (int a = 0; a < agentsNum; a++) {
                int ind = a % cl_pok.size();
                CL_Pokemon c = cl_pok.get(ind);

                startPos(a, c);

            }


        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    /**
     * Positions the agents for the first time according
     * to the location of the Pokemon
     * @param id agent id
     * @param c pokemon target
     */
    private void startPos(int id, CL_Pokemon c) {

        if(g.getE(c.getDest()).size()==0){
            int nextKey=chooseRanDest();
            game.addAgent(nextKey);
        }

        else {
            game.addAgent(c.getSrc());
        }

        List<node_data> newList = new ArrayList<>();
        path.put(id, newList);


    }

    /**
     * Returns random node the agent go to.
     * @return node key
     */
    private static int chooseRanDest() {
        int num_of_nodes = g.nodeSize();
        int rand = (int) (Math.random() * num_of_nodes);
        return rand;
    }

    /**
     * Saves the graph as Json file.
     * @param g String of the graph
     * @return Json file
     */
    private String save(String g) {
        try {
            FileWriter fw = new FileWriter("Jgraph.json");
            fw.write(g);
            fw.flush();
            fw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Jgraph.json";
    }

    /**
     * Moves each of the agents along the edge,
     * in case the agent is on a node the next destination next edge is chosen .
     * In case the agent reached the destination a new destination was chosen for him
     */
    private static void moveAgants() {

        //update the game data.
        String lg = game.move();
        List<CL_Agent> agents = Arena.getAgents(lg, g);
        _ar.setAgents(agents);
        String fs = game.getPokemons();
        List<CL_Pokemon> ffs = Arena.json2Pokemons(fs);


        for (int a = 0; a < ffs.size(); a++) {
            Arena.updateEdge(ffs.get(a), g);
        }

        change=false;

        /* checking if there was any change on agents.
        change==arrived to the destination
        */
        for (CL_Agent agent : agents) {
            if (agent.getNextNode()== -1 && path.get(agent.getID()).isEmpty()) {
                change = true;
                break;
            }
        }

        //if there was any change--> Choose a new target for all the Pokemon
        if (change || firstRun) {
            _ar.setPokemons(ffs);
            for (CL_Agent agent : agents) {


                path.put(agent.getID(), new ArrayList<>());
                chooseNext(agent);

            }
            firstRun=false;
        }


        for (CL_Agent agent : agents) {

            if (agent.getNextNode() == -1) {
                int id = agent.getID();
                double v = agent.getValue();

                //get the next node in the shortest path.
                int nextKey= path.get(id).get(0).getKey();
                agent.setNextNode(nextKey);

                game.chooseNextEdge(agent.getID(), nextKey);

                String infoAg="Agent: " + agent.getID() + ", Grade: " + agent.getValue();
                _ar.set_info(infoAg, id);


                System.out.println("Agent: " + id + ", val: " + v + "   turned to node: " + nextKey);

                path.get(id).remove(0);

            }


        }
    }



    /**
     * This function checks which pokemon should send the Agent
     * by calculating to which Pokemon the shortest path
     *
     * @param ag the Agent
     */
    private static void chooseNext(CL_Agent ag) {

        dw_graph_algorithms ga = new DWGraph_Algo();
        ga.init(g);

        List<node_data> list = new ArrayList<>();

        double minDist = Double.MAX_VALUE;
        CL_Pokemon chosen = null;
        int agSrc = ag.getSrcNode();

        for (CL_Pokemon c : _ar.getPokemons())
        {
            if (!c.getIsVisit()) { //there isn't any agent who choose this pokemon.

                double currDist = ga.shortestPathDist(agSrc, c.getSrc());

                //////
                if(g.getE(c.getDest()).size()==0|| currDist== -1){
                    break;
                }

                //Save the shortest route
                if (currDist < minDist) {

                    list.clear();
                    minDist = currDist;
                    chosen = c;
                    list.addAll(ga.shortestPath(agSrc, c.getSrc()));
                    list.add(g.getNode(c.getDest()));

                    //if currDist==0 ,this is definitely the shortest way
                    if (currDist == 0)
                    {
                        break;
                    }
                }
            }
        }

        if(chosen != null) {
            chosen.setIsVisit(true);
        }

        path.put(ag.getID(),list);
        target.put(ag.getID(),chosen);

    }


}