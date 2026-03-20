package src.pas.uno.agents;


// SYSTEM IMPORTS
import edu.bu.pas.uno.Card;
import edu.bu.pas.uno.Game.GameView;
import edu.bu.pas.uno.Game;
import edu.bu.pas.uno.Game.PlayerOrder;
import edu.bu.pas.uno.Hand.HandView;
import edu.bu.pas.uno.Hand;
import edu.bu.pas.uno.agents.MCTSAgent;
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
            GameView game = this.getGameView();
            Game new_game = new Game(game);

            if (move != null){
                new_game.resolveMove(move);
            }

            return new MCTSNode(new_game.getView(this.getLogicalPlayerIdx()), this.getLogicalPlayerIdx(), this.getParent());
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
    // public double GetUtility(Node node, GameView game){ //Get the utility of the current child
    //     double pr_a_given_s = Math.pow((node.getQValue(getLogicalPlayerIdx()) / node.getQValueTotal(getLogicalPlayerIdx())), 2);
    //     double fin = pr_a_given_s * node.getQValue(getLogicalPlayerIdx());

    //     return fin;
    // }

    public Boolean explore(Node node){
        System.out.println("in Explore");
        GameView game = node.getGameView();

        if(game.isOver()){
            HandView hand = game.getHandView(getLogicalPlayerIdx());
            System.out.println("Explore, game over");
            if(hand.size() == 0){
                return true;
            }
            else{
                return false;
            }
        }

        else{
            System.out.println("Explore: Game not over");
            NodeState state = node.getNodeState();
            Random random = new Random();

            if(state.equals(NodeState.NO_LEGAL_MOVES_UNRESOLVED_CARDS_PRESENT)){
                System.out.println("Explore: Unresolved cards");
                //one move, just draw cards
                Agent agent = this;
                Move move;
                move = Move.createMove(agent, Node.NoLegalMovesIdxDefaults.DrawUnresolvedCardsIdxs.MOVE_IDX);
                Node child = node.getChild(move);
                if (explore(child)){
                    return true;
                }
                else{
                    return false;
                }
            }
            else if(state.equals(NodeState.NO_LEGAL_MOVES_MAY_PLAY_DRAWN_CARD)){
                System.out.println("Explore: may play drawn card");
                Boolean coin_flip = random.nextBoolean();
                Agent agent = this;
                Move move = null;
                if(coin_flip){
                   move = Move.createMove(agent, Node.NoLegalMovesIdxDefaults.DrawSingleCardIdxs.KEEP_CARD_MOVE_IDX); 
                }
                else{
                    HandView hand = game.getHandView(getLogicalPlayerIdx());
                    Hand h = new Hand(hand);
                    Card card = hand.getCard(Node.NoLegalMovesIdxDefaults.DrawSingleCardIdxs.PLAY_CARD_MOVE_IDX);
                    boolean iswild = card.isWild();


                if(iswild){
                    System.out.println("Explore: legalmoves, iswild");
                    List<Card> cur_hand = new ArrayList<>();
                    cur_hand = h.getCards();
                    int red = 0;
                    int yellow = 0;
                    int blue = 0;
                    int green = 0;
                    for(Card c : cur_hand){
                        if(c.color() == Color.RED){
                            red += 1;
                        }
                        if(c.color() == Color.YELLOW){
                            yellow += 1;
                        }
                        if(c.color() == Color.BLUE){
                            blue += 1;
                        }
                        if(c.color() == Color.GREEN){
                            green += 1;
                        }
                    }

                    if (red >= yellow && red >= blue && red >= green){
                        move = Move.createMove(this, Node.NoLegalMovesIdxDefaults.DrawSingleCardIdxs.PLAY_CARD_MOVE_IDX, Color.RED);
                    }
                    if (yellow >= red && yellow >= blue && yellow >= green){
                        move = Move.createMove(this, Node.NoLegalMovesIdxDefaults.DrawSingleCardIdxs.PLAY_CARD_MOVE_IDX, Color.YELLOW);
                    }
                    if (blue >= yellow && blue >= red && blue >= green){
                        move = Move.createMove(this, Node.NoLegalMovesIdxDefaults.DrawSingleCardIdxs.PLAY_CARD_MOVE_IDX, Color.BLUE);
                    }
                    if (green >= yellow && green >= blue && green >= red){
                        move = Move.createMove(this, Node.NoLegalMovesIdxDefaults.DrawSingleCardIdxs.PLAY_CARD_MOVE_IDX, Color.GREEN);
                    }
                    Node child = node.getChild(move);
                    if (explore(child)){
                        return true;
                    }
                    else{
                        return false;
                    }
                }else{
                    System.out.println("Explore: Legal moves, not wild");
                    move = Move.createMove(this, Node.NoLegalMovesIdxDefaults.DrawSingleCardIdxs.PLAY_CARD_MOVE_IDX);
                    Node child = node.getChild(move);
                    if (explore(child)){
                        return true;
                    }
                    else{
                        return false;
                    }
                }
                }
                
            }

            //There are legal moves
            else{
                System.out.println("Explore: Legal Moves");

                List<Integer> legal = new ArrayList<>();
                legal = node.getOrderedLegalMoves();
                int randomindex = random.nextInt(legal.size());

                HandView hand = game.getHandView(getLogicalPlayerIdx());
                Hand h = new Hand(hand);
                Card card = hand.getCard(Node.NoLegalMovesIdxDefaults.DrawSingleCardIdxs.PLAY_CARD_MOVE_IDX);
                boolean iswild = card.isWild();

                if(iswild){
                    System.out.println("Explore: legalmoves, iswild");
                    List<Card> cur_hand = new ArrayList<>();
                    cur_hand = h.getCards();
                    int red = 0;
                    int yellow = 0;
                    int blue = 0;
                    int green = 0;
                    for(Card c : cur_hand){
                        if(c.color() == Color.RED){
                            red += 1;
                        }
                        if(c.color() == Color.YELLOW){
                            yellow += 1;
                        }
                        if(c.color() == Color.BLUE){
                            blue += 1;
                        }
                        if(c.color() == Color.GREEN){
                            green += 1;
                        }
                    }
                    Move move = null;
                    if (red >= yellow && red >= blue && red >= green){
                        move = Move.createMove(this, randomindex, Color.RED);
                    }
                    if (yellow >= red && yellow >= blue && yellow >= green){
                        move = Move.createMove(this, randomindex, Color.YELLOW);
                    }
                    if (blue >= yellow && blue >= red && blue >= green){
                        move = Move.createMove(this, randomindex, Color.BLUE);
                    }
                    if (green >= yellow && green >= blue && green >= red){
                        move = Move.createMove(this, randomindex, Color.GREEN);
                    }
                    Node child = node.getChild(move);
                    if (explore(child)){
                        return true;
                    }
                    else{
                        return false;
                    }
                }else{
                    System.out.println("Explore: Legal moves, not wild");
                    Move move = Move.createMove(this, randomindex);
                    Node child = node.getChild(move);
                    if (explore(child)){
                        return true;
                    }
                    else{
                        return false;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public Node search(final GameView game,
                       final Integer drawnCardIdx)
    {
        System.out.println("in search");
        MCTSNode node = new MCTSNode(game, getLogicalPlayerIdx(), null);
        long start = System.currentTimeMillis();


        while(System.currentTimeMillis() - start < 5000){
        Random random = new Random();
        NodeState state = node.getNodeState();
        Move move = null; //Initialize move
        int random_idx;
        if(state.equals(NodeState.NO_LEGAL_MOVES_UNRESOLVED_CARDS_PRESENT)){
            move = Move.createMove(this, Node.NoLegalMovesIdxDefaults.DrawUnresolvedCardsIdxs.MOVE_IDX);
            random_idx = Node.NoLegalMovesIdxDefaults.DrawUnresolvedCardsIdxs.MOVE_IDX;
        }
        else if(state.equals(NodeState.NO_LEGAL_MOVES_MAY_PLAY_DRAWN_CARD)){
            Boolean coin = random.nextBoolean();
            if(coin){
                
                
                HandView hand = game.getHandView(getLogicalPlayerIdx());
                Hand h = new Hand(hand);
                Card card = hand.getCard(Node.NoLegalMovesIdxDefaults.DrawSingleCardIdxs.PLAY_CARD_MOVE_IDX);
                boolean iswild = card.isWild();

                if(iswild){
                    List<Card> cur_hand = new ArrayList<>();
                    cur_hand = h.getCards();
                    int red = 0;
                    int yellow = 0;
                    int blue = 0;
                    int green = 0;
                    for(Card c : cur_hand){
                        if(c.color() == Color.RED){
                            red += 1;
                        }
                        if(c.color() == Color.YELLOW){
                            yellow += 1;
                        }
                        if(c.color() == Color.BLUE){
                            blue += 1;
                        }
                        if(c.color() == Color.GREEN){
                            green += 1;
                        }
                    }
                    if (red >= yellow && red >= blue && red >= green){
                        move = Move.createMove(this, Node.NoLegalMovesIdxDefaults.DrawSingleCardIdxs.PLAY_CARD_MOVE_IDX, Color.RED);         
                    }
                    if (yellow >= red && yellow >= blue && yellow >= green){
                        move = Move.createMove(this, Node.NoLegalMovesIdxDefaults.DrawSingleCardIdxs.PLAY_CARD_MOVE_IDX, Color.YELLOW);           
                    }
                    if (blue >= yellow && blue >= red && blue >= green){
                        move = Move.createMove(this, Node.NoLegalMovesIdxDefaults.DrawSingleCardIdxs.PLAY_CARD_MOVE_IDX, Color.BLUE);               
                    }
                    if (green >= yellow && green >= blue && green >= red){
                        move = Move.createMove(this, Node.NoLegalMovesIdxDefaults.DrawSingleCardIdxs.PLAY_CARD_MOVE_IDX, Color.GREEN);
                    }
                }
                else{
                    move = Move.createMove(this, Node.NoLegalMovesIdxDefaults.DrawSingleCardIdxs.PLAY_CARD_MOVE_IDX);
                }
                random_idx = Node.NoLegalMovesIdxDefaults.DrawSingleCardIdxs.PLAY_CARD_MOVE_IDX;
            }
            else{
                move = Move.createMove(this, Node.NoLegalMovesIdxDefaults.DrawSingleCardIdxs.KEEP_CARD_MOVE_IDX);
                random_idx = Node.NoLegalMovesIdxDefaults.DrawSingleCardIdxs.KEEP_CARD_MOVE_IDX;
            }
        }
        else{
            List<Integer> legal = node.getOrderedLegalMoves();
            int randomidx = random.nextInt(legal.size());
            random_idx = randomidx;

            HandView hand = game.getHandView(getLogicalPlayerIdx());
            Hand h = new Hand(hand);
            Card card = hand.getCard(randomidx);
            boolean iswild = card.isWild();

            if(iswild){
                List<Card> cur_hand = new ArrayList<>();
                cur_hand = h.getCards();
                int red = 0;
                int yellow = 0;
                int blue = 0;
                int green = 0;
                for(Card c : cur_hand){
                    if(c.color() == Color.RED){
                        red += 1;
                    }
                    if(c.color() == Color.YELLOW){
                        yellow += 1;
                    }
                    if(c.color() == Color.BLUE){
                        blue += 1;
                    }
                    if(c.color() == Color.GREEN){
                        green += 1;
                    }
                }
                if (red >= yellow && red >= blue && red >= green){
                    move = Move.createMove(this, randomidx, Color.RED);         
                }
                if (yellow >= red && yellow >= blue && yellow >= green){
                    move = Move.createMove(this, randomidx, Color.YELLOW);           
                }
                if (blue >= yellow && blue >= red && blue >= green){
                    move = Move.createMove(this, randomidx, Color.BLUE);               
                }
                if (green >= yellow && green >= blue && green >= red){
                    move = Move.createMove(this, randomidx, Color.GREEN);
                }
            }
            else{
                move = Move.createMove(this, randomidx);
            }

        }
        //After Checking the state make the random child and explore it
        Node child = node.getChild(move);
        if(explore(child)){
            node.setQValueTotal(random_idx, node.getQValueTotal(random_idx) + 1);
            node.setQCount(random_idx, node.getQCount(random_idx) + 1);
        }
        else{
            node.setQCount(random_idx, node.getQCount(random_idx) + 1);
        }
        System.out.println(node.getQCount(random_idx) + "--------------------------------");
        System.out.println(node.getQValueTotal(random_idx) + "--------------------------------");
        }
        System.out.println("Done search");
        return node;
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
        System.out.println("In argmax");
        List<Integer> legal_moves = new ArrayList<>();
        legal_moves = node.getOrderedLegalMoves();

        //Start by check the state of this node
        NodeState state = node.getNodeState();
        if(state.equals(NodeState.NO_LEGAL_MOVES_UNRESOLVED_CARDS_PRESENT)){
            //one move, just draw cards
            Agent agent = this;
            Move move;
            move = Move.createMove(agent, Node.NoLegalMovesIdxDefaults.DrawUnresolvedCardsIdxs.MOVE_IDX);
            return move;
        }

        else if(state.equals(NodeState.NO_LEGAL_MOVES_MAY_PLAY_DRAWN_CARD)){
            //two moves, 1. Keep the card, 2.a. Use the card, 2.b. Use the card but its wild
            if(node.getQValue(Node.NoLegalMovesIdxDefaults.DrawSingleCardIdxs.KEEP_CARD_MOVE_IDX) < node.getQValue(Node.NoLegalMovesIdxDefaults.DrawSingleCardIdxs.PLAY_CARD_MOVE_IDX)){
                Agent agent = this;
                Move move;
                GameView gameview = node.getGameView();
                HandView hand = gameview.getHandView(getLogicalPlayerIdx());
                Hand h = new Hand(hand);
                Card card = hand.getCard(Node.NoLegalMovesIdxDefaults.DrawSingleCardIdxs.PLAY_CARD_MOVE_IDX);
                boolean iswild = card.isWild();
                if(iswild){
                    List<Card> cur_hand = new ArrayList<>();
                    cur_hand = h.getCards();
                    int red = 0;
                    int yellow = 0;
                    int blue = 0;
                    int green = 0;
                    for(Card c : cur_hand){
                        if(c.color() == Color.RED){
                            red += 1;
                        }
                        if(c.color() == Color.YELLOW){
                            yellow += 1;
                        }
                        if(c.color() == Color.BLUE){
                            blue += 1;
                        }
                        if(c.color() == Color.GREEN){
                            green += 1;
                        }
                    }
                    if (red >= yellow && red >= blue && red >= green){
                        move = Move.createMove(agent, Node.NoLegalMovesIdxDefaults.DrawSingleCardIdxs.PLAY_CARD_MOVE_IDX, Color.RED);
                        return move;
                    }
                    if (yellow >= red && yellow >= blue && yellow >= green){
                        move = Move.createMove(agent, Node.NoLegalMovesIdxDefaults.DrawSingleCardIdxs.PLAY_CARD_MOVE_IDX, Color.YELLOW);
                        return move;
                    }
                    if (blue >= yellow && blue >= red && blue >= green){
                        move = Move.createMove(agent, Node.NoLegalMovesIdxDefaults.DrawSingleCardIdxs.PLAY_CARD_MOVE_IDX, Color.BLUE);
                        return move;
                    }
                    if (green >= yellow && green >= blue && green >= red){
                        move = Move.createMove(agent, Node.NoLegalMovesIdxDefaults.DrawSingleCardIdxs.PLAY_CARD_MOVE_IDX, Color.GREEN);
                        return move;
                    }
                }else{  
                    move = Move.createMove(agent, Node.NoLegalMovesIdxDefaults.DrawSingleCardIdxs.PLAY_CARD_MOVE_IDX);
                    return move;
                }
            }
            else{
                Agent agent = this;
                Move move;
                move = Move.createMove(agent, Node.NoLegalMovesIdxDefaults.DrawSingleCardIdxs.KEEP_CARD_MOVE_IDX);
                return move;
            }
            
        }
        else{
            //Possibly many moves
            //Get the best move
            Integer idx = -1;
            for (Integer i : legal_moves){
                if (node.getQValue(i) > idx){
                    idx = i;
                }
            }
            Agent agent = this;
            Move move;
            //Check if card is wild
            GameView gameview = node.getGameView();
            HandView hand = gameview.getHandView(getLogicalPlayerIdx());
            Hand h = new Hand(hand);
            Card card = hand.getCard(idx);
            boolean iswild = card.isWild();
            if(iswild){
                //If wild, determine the largest number of colors in your hand and set the wild to that color
                List<Card> cur_hand = new ArrayList<>();
                cur_hand = h.getCards();
                int red = 0;
                int yellow = 0;
                int blue = 0;
                int green = 0;
                for(Card c : cur_hand){
                    if(c.color() == Color.RED){
                        red += 1;
                    }
                    if(c.color() == Color.YELLOW){
                        yellow += 1;
                    }
                    if(c.color() == Color.BLUE){
                        blue += 1;
                    }
                    if(c.color() == Color.GREEN){
                        green += 1;
                    }
                }
                if (red >= yellow && red >= blue && red >= green){
                    move = Move.createMove(agent, idx, Color.RED);
                    return move;
                }
                if (yellow >= red && yellow >= blue && yellow >= green){
                    move = Move.createMove(agent, idx, Color.YELLOW);
                    return move;
                }
                if (blue >= yellow && blue >= red && blue >= green){
                    move = Move.createMove(agent, idx, Color.BLUE);
                    return move;
                }
                if (green >= yellow && green >= blue && green >= red){
                    move = Move.createMove(agent, idx, Color.GREEN);
                    return move;
                }
            }else{  
                //Else just play the card
                move = Move.createMove(agent, idx);
                return move;
            }

        }
        System.err.println("Something wrong in argmax values");
        return null;
    }
}
