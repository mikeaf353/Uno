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
    public double GetUtility(Node node, GameView game){ //Get the utility of the current child
        double pr_a_given_s = Math.pow((node.getQValue(getLogicalPlayerIdx()) / node.getQValueTotal(getLogicalPlayerIdx())), 2);
        double fin = pr_a_given_s * node.getQValue(getLogicalPlayerIdx());

        return fin;
    }

    // public Node AlphaBeta( Node node, double alpha, double beta, GameView game){
    //     Node best_node = null;
    //     if (node.isTerminal()){
    //         //node.setUtilityValue(node.getTerminalUtility());
    //         return node;
    //     }
    //     if(node.getDepth() == 3){
    //         //I think I run simulations and update Q values
    //         return node;
    //     }
    //     PlayerOrder order = game.getPlayerOrder();
    //     if(order.getCurrentAgentIdx() == getPlayerIdx()){
    //         best_node  = node;
    //         double max_eval = Double.NEGATIVE_INFINITY;
    //         List<Integer> legal_moves = new ArrayList<>();
    //         legal_moves = node.getOrderedLegalMoves();
    //         for (Integer l : legal_moves){
    //             Move move = Move.createMove(this, l);
    //             Node child = node.getChild(move);
    //             GameView cg = child.getGameView();
    //             Node eval = AlphaBeta(child, alpha, beta, cg);
    //             if(GetUtility(eval, game) > max_eval){
    //                 max_eval = GetUtility(best_node, game);
    //                 best_node = child;
    //             }
    //             if (max_eval > beta){
    //                 //update nodes Qvalues here I think
    //                 // Reason being this is where we normally update the parents utiltiy
    //                 return best_node;
    //             }
    //             alpha = Math.max(alpha, GetUtility(eval, game));
    //         }
    //         //set node utility again to max eval some how
    //         return best_node;
    //     }
    //     else{

    //     }
    //     return null;
    // }

    public Boolean explore(Node node){
        GameView game = node.getGameView();

        if(game.isOver()){
            HandView hand = game.getHandView(getLogicalPlayerIdx());
            if(hand.size() == 0){
                node.setQCount(game.getCurrentMoveIdx(), node.getQCount(game.getCurrentMoveIdx()) + 1);
                node.setQValueTotal(game.getCurrentMoveIdx(), node.getQValueTotal(game.getCurrentMoveIdx()) + 1);
                return true;
            }
            else{
                node.setQCount(game.getCurrentMoveIdx(), node.getQCount(game.getCurrentMoveIdx()) + 1);
                return false;
            }
        }
        else{
        Random random = new Random();
        List<Integer> legal = new ArrayList<>();
        legal = node.getOrderedLegalMoves();
        int random_index = random.nextInt(legal.size());
        Integer random_int = legal.get(random_index);


        Move move = Move.createMove(this, random_int);
        Node child = node.getChild(move);
        if (explore(child)){
            node.setQCount(random_index, node.getQCount(random_index) + 1);
            node.setQValueTotal(random_index, node.getQValueTotal(random_index) + 1);
            return true;
        }
        else{
            node.setQCount(random_index, node.getQCount(random_index) + 1);
            return false;
        }

        }
    }



    @Override
    public Node search(final GameView game,
                       final Integer drawnCardIdx)
    {
        MCTSNode node = new MCTSNode(game, getLogicalPlayerIdx(), null);
        long start = System.currentTimeMillis();
        while(System.currentTimeMillis() - start < 2000){
            explore(node);
        }
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
                    if (red > yellow && red > blue && red > green){
                        move = Move.createMove(agent, Node.NoLegalMovesIdxDefaults.DrawSingleCardIdxs.PLAY_CARD_MOVE_IDX, Color.RED);
                        return move;
                    }
                    if (yellow > red && yellow > blue && yellow > green){
                        move = Move.createMove(agent, Node.NoLegalMovesIdxDefaults.DrawSingleCardIdxs.PLAY_CARD_MOVE_IDX, Color.YELLOW);
                        return move;
                    }
                    if (blue > yellow && blue > red && blue > green){
                        move = Move.createMove(agent, Node.NoLegalMovesIdxDefaults.DrawSingleCardIdxs.PLAY_CARD_MOVE_IDX, Color.BLUE);
                        return move;
                    }
                    if (green > yellow && green > blue && green > red){
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
                if (red > yellow && red > blue && red > green){
                    move = Move.createMove(agent, idx, Color.RED);
                    return move;
                }
                if (yellow > red && yellow > blue && yellow > green){
                    move = Move.createMove(agent, idx, Color.YELLOW);
                    return move;
                }
                if (blue > yellow && blue > red && blue > green){
                    move = Move.createMove(agent, idx, Color.BLUE);
                    return move;
                }
                if (green > yellow && green > blue && green > red){
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
