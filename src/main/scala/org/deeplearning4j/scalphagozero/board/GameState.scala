package org.deeplearning4j.scalphagozero.board

import org.deeplearning4j.scalphagozero.scoring.GameResult

/**
  * GameState encodes the state of a game of Go. Game states have board instances,
  * but also track previous moves to assert validity of moves etc. GameState is
  * immutable, i.e. after you apply a move a new GameState instance will be returned.
  *
  * @param board a GoBoard instance
  * @param nextPlayer the Player who is next to play
  * @param previousState Previous GameState, if any
  * @param lastMove last move played in this game, if any
  * @author Max Pumperla
  */
case class GameState(
    board: GoBoard,
    nextPlayer: Player,
    previousState: Option[GameState] = None,
    lastMove: Option[Move] = None,
    allPreviousStates: Set[(Player, Long)] = Set.empty
) {

  /** true when the game is over - when someone resigns or there are 2 consecutive passes */
  val isOver: Boolean =
    lastMove match {
      case None | Some(Move.Play(_)) => false
      case Some(Move.Resign)         => true
      case Some(Move.Pass) =>
        val secondLastMove = previousState.get.lastMove
        secondLastMove match {
          case Some(Move.Pass)                               => true
          case None | Some(Move.Play(_)) | Some(Move.Resign) => false
        }
    }

  def applyMove(move: Move): GameState = {
    val nextBoard: GoBoard =
      move match {
        case Move.Play(point)        => board.placeStone(nextPlayer, point)
        case Move.Pass | Move.Resign => board
      }

    val newAllPrevStates = allPreviousStates + (nextPlayer -> nextBoard.zobristHash)
    new GameState(nextBoard, nextPlayer.other, Some(this), Some(move), newAllPrevStates)
  }

  /** @return true if the move commits suicide for the group. Suicide is not allowed */
  def isMoveSelfCapture(player: Player, move: Move): Boolean =
    move match {
      case Move.Pass | Move.Resign => false
      case Move.Play(point)        => board.isSelfCapture(player, point)
    }

  /** @return true if the move would take back a ko that was just taken. That is not allowed */
  def doesMoveViolateKo(player: Player, move: Move): Boolean = {
    var nextBoard = board
    move match {
      case Move.Play(point) if board.willCapture(player, point) =>
        nextBoard = nextBoard.placeStone(player, point)
        val nextSituation = player -> nextBoard.zobristHash
        allPreviousStates.contains(nextSituation)
      case _ => false
    }
  }

  /** @return true if the move fills a single point eye. Though not strictly illegal, it is never a good idea */
  def doesMoveFillEye(player: Player, move: Move): Boolean =
    move match {
      case Move.Pass | Move.Resign => false
      case Move.Play(point)        => board.doesMoveFillEye(player, point)
    }

  def isValidMove(move: Move): Boolean =
    if (isOver) false
    else {
      move match {
        case Move.Resign | Move.Pass => true
        case Move.Play(point) =>
          board.getPlayer(point).isEmpty &&
          !isMoveSelfCapture(nextPlayer, move) &&
          !doesMoveViolateKo(nextPlayer, move) &&
          !doesMoveFillEye(nextPlayer, move)
      }
    }

  def gameResult(komi: Float = 0.5f): Option[GameResult] =
    if (isOver) {
      lastMove match {
        case Some(Move.Resign)                           => Some(GameResult(board, komi, Some(nextPlayer)))
        case None | Some(Move.Play(_)) | Some(Move.Pass) => Some(GameResult(board, komi))
      }
    } else None
}

object GameState {

  def newGame(boardSize: Int): GameState = {
    val board = GoBoard(boardSize)
    new GameState(board, BlackPlayer)
  }
}
