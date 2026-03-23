package src.pas.uno.agents;


// SYSTEM IMPORTS
import edu.bu.pas.uno.Card;
import edu.bu.pas.uno.Game.GameView;
import edu.bu.pas.uno.Game;
import edu.bu.pas.uno.Game.PlayerOrder;
import edu.bu.pas.uno.Hand.HandView;
import edu.bu.pas.uno.Hand;
import edu.bu.pas.uno.agents.MCTSAgent;
import edu.bu.pas.uno.agents.RandomAgent;
import edu.bu.pas.uno.agents.Agent;
import edu.bu.pas.uno.enums.Color;
import edu.bu.pas.uno.enums.Value;
import edu.bu.pas.uno.moves.Move;
import edu.bu.pas.uno.tree.Node;
import edu.bu.pas.uno.tree.Node.NodeState;

import java.util.ArrayList;
import java.util.Random;
import java.util.Set;
import java.util.List;
import java.util.Stack;


// JAVA PROJECT IMPORTS


public class ExpectedOutcomeAgent
    extends MCTSAgent
{

    public static class MCTSNode
        extends Node
    {
        public MCTSNode(final GameView game,
                        final int logicalPlayerIdx,
                        final Node parent)
        {
            super(game, logicalPlayerIdx, parent);
        }

        @Override
        public Node getChild(final Move move)
        {
            GameView gameView = this.getGameView();
            //Creating rando agents, and adding them to make a new game instance
            Agent[] agentList = new Agent[gameView.getNumPlayers()];
            for(int i = 0; i < gameView.getNumPlayers(); i++) {
                int agentIdx = gameView.getPlayerOrder().getAgentIdx(i);
                //Set thinking time to 5000 for now. Might need to change
                Agent a = new RandomAgent(agentIdx, 5000);
                a.setLogicalPlayerIdx(i);
                agentList[i] = a;
            }
            Game game = new Game(gameView, agentList);

            if(move != null){
                game.resolveMove(move);
            }

            return new MCTSNode(game.getView(this.getLogicalPlayerIdx()), this.getLogicalPlayerIdx(), this.getParent());
        }
    }
    public ExpectedOutcomeAgent(final int playerIdx,
                                final long maxThinkingTimeInMS)
    {
        super(playerIdx, maxThinkingTimeInMS);
    }
    
    /**
     * A method to perform the MCTS search on the game tree
     *
     * @param   game            The {@link GameView} that should be the root of the game tree
     * @param   drawnCardIdx    This will be non-null when this method is being called by the 
     *                          <code>maybePlayDrawnCard</code> method of {@link Agent} and will
     *                          be <code>null</code> when being called by <code>chooseCardToPlay</code>
     *                          method of {@link Agent}
     * @return  The {@link Node} of the root who'se q-values should now be populated and ready to argmax
     */



    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// 
    /// 
    public Color getWildColor(Color curColor) {
        ArrayList<Color> colors = new ArrayList<>();
        colors.add(Color.RED);
        colors.add(Color.YELLOW);
        colors.add(Color.GREEN);
        colors.add(Color.BLUE);
        colors.remove(curColor);
        
        int idx = getRandom().nextInt(colors.size());
        return colors.get(idx);
    }

    public List<Move> process_unresolved(Node node, Agent agent){
        List<Move> moves = new ArrayList<>();
        Move move = Move.createMove(agent, Node.NoLegalMovesIdxDefaults.DrawUnresolvedCardsIdxs.MOVE_IDX);
        moves.add(move);
        return moves;
    }
    public List<Move> process_may_draw(Node node, Agent agent){ 
        List<Move> moves = new ArrayList<>();
        GameView game = node.getGameView();
        HandView hand = game.getHandView(getLogicalPlayerIdx());
        Card card = hand.getCard(Node.NoLegalMovesIdxDefaults.DrawSingleCardIdxs.PLAY_CARD_MOVE_IDX);
        Move move1 = Move.createMove(agent, Node.NoLegalMovesIdxDefaults.DrawSingleCardIdxs.KEEP_CARD_MOVE_IDX);
        moves.add(move1);
        if(card.isWild()){
            Color color = getWildColor(game.getCurrentColor());
            Move move2 = Move.createMove(agent, Node.NoLegalMovesIdxDefaults.DrawSingleCardIdxs.PLAY_CARD_MOVE_IDX, color);
            moves.add(move2);
            return moves;
        }
        else{
            Move move2 = Move.createMove(agent, Node.NoLegalMovesIdxDefaults.DrawSingleCardIdxs.PLAY_CARD_MOVE_IDX);
            moves.add(move2);
            return moves;
        }
    }
    public List<Move> process_legal(Node node, Agent agent){
        List<Move> moves = new ArrayList<>();
        List<Integer> legal = node.getOrderedLegalMoves();
        GameView game = node.getGameView();
        HandView hand = game.getHandView(agent.getLogicalPlayerIdx());


        for (int i = 0; i < legal.size(); i++){
            Card card = hand.getCard(legal.get(i));
            if(card.isWild()){
                Color color = getWildColor(game.getCurrentColor());
                Move move2 = Move.createMove(agent, legal.get(i), color);
                moves.add(move2);
            }
            else{
                Move move2 = Move.createMove(agent, legal.get(i));
                moves.add(move2);
            }
        }
        return moves;
        //Moves is returning properly for legal
    }

    public long rollout(Node n){
        PlayerOrder order = n.getGameView().getPlayerOrder();
        int num = n.getGameView().getNumPlayers();
        Agent[] agents = new Agent[num];

        for(int i = 0; i < num; i++){
            int agentIdx = order.getAgentIdx(i);
            RandomAgent rand = new RandomAgent(agentIdx, getMaxThinkingTimeInMS());
            rand.setLogicalPlayerIdx(i);
            agents[i] = rand;
        }

        Game game = new Game(n.getGameView(), agents);

        while(!game.isOver()) {
            Move move = game.getMove();
            game.resolveMove(move);
        }

        HandView hand = game.getView(getLogicalPlayerIdx()).getHandView(getLogicalPlayerIdx());
        Hand h = new Hand(hand);
        List<Card> cards = new ArrayList<>();
        cards = h.getCards();
        if(cards.size() == 0) {
            return 1;
        } else return 0;
    }
    


    @Override
    public Node search(final GameView game,
                       final Integer drawnCardIdx)
    {
        MCTSNode root = new MCTSNode(game, getLogicalPlayerIdx(), null);
        Random random = getRandom();
        NodeState state = root.getNodeState();
        List<Move> moves = new ArrayList<>();
        if(state.equals(Node.NodeState.NO_LEGAL_MOVES_UNRESOLVED_CARDS_PRESENT)){
            return root;
        }
        else if(state.equals(Node.NodeState.NO_LEGAL_MOVES_MAY_PLAY_DRAWN_CARD)){
            moves = process_may_draw(root, this);
        }
        else{
            moves = process_legal(root, this);
        }
        int idx = 0;
        for(Move m : moves){
            Node c = root.getChild(m);
            for(int i = 0; i < 50; i++){
                long win = rollout(c);
                root.setQValueTotal(idx, root.getQValueTotal(idx) + win);
                root.setQCount(idx, root.getQCount(idx) + 1);
            }
            idx++;
        }

        return root;
    }  
    //////////////////////////////////////////////////////////
    /**
     * A method to argmax the Q values inside a {@link Node}
     *
     * @param   node            The {@link Node} who has populated q-values
     * @return  The {@link Move} corresponding to whichever {@link Move} has the largest q-value. Note
     *          that this can be <code>null</code> if you choose to not play the drawn card (you will
     *          have to detect whether or not you are in that scenario by examining the @{link Node}'s state).
     */
    @Override
    public Move argmaxQValues(final Node node)
    {
        NodeState state = node.getNodeState();
        List<Move> moves = new ArrayList<>();

        //Setting the moves based on nodeState
        if(state.equals(Node.NodeState.NO_LEGAL_MOVES_UNRESOLVED_CARDS_PRESENT)) {
            moves = process_unresolved(node, this);
        } else if(state.equals(Node.NodeState.NO_LEGAL_MOVES_MAY_PLAY_DRAWN_CARD)) {
            moves = process_may_draw(node, this);
        } else {
            moves = process_legal(node, this);
        }

        //Basic score maxxer that finds the idx of the best move to play
        int bestIdx = 0;
        double bestScore = -1.0;
        for(int i = 0; i < moves.size(); i++) {
            long qCount = node.getQCount(i);
            double score = 0.0;
            if(qCount > 0) {
                score = ((double) node.getQValueTotal(i)) / qCount;
            }
            if(score > bestScore) {
                bestScore = score;
                bestIdx = i;
            }
        }
        return moves.get(bestIdx);
    }
}
