package net.ssanj.dabble

import scala.io.StdIn.readLine
import scala.util.Try

import scalaz.{Success, \/, -\/, \/-}
import scalaz.syntax.either._

object DabbleApp extends DependencyParser  with
                         ResolverParser    with
                         DabblePrinter     with
                         DefaultTemplate   with
                         Executor          with
                         DabblePaths       with
                         DabbleHistory     with
                         Banner            with
                         TerminalSupport {


   //TODO: Make this safe, use: Try
   //Move logic into a separate module.
   //TODO: 1. remove duplicate history entries
   //TODO: 2. add searching across history
  def main(args: Array[String]) {
    parser.parse(args, DabbleRunConfig()) match {
      case Some(DabbleRunConfig(deps, res, mp, None)) =>
        getBanner.foreach(println)

        val result =
              (for {
                d <- parseDependencies(deps)
                r <- (if (res.nonEmpty) parseResolvers(res)
                      else Seq.empty.right[String])
                //TODO: Create a DabbleConfig from DabbleRunConfig which has valid types.
              } yield (d, r, mp)).fold(processingFailed, (build _).tupled)


        exit(result)
      case Some(DabbleRunConfig(_, _, _, Some(HistoryCommand(searchTerm)))) =>
        val historyLinesRead = readHistoryFile().collect { case Success(line) => line }
        import scala.collection.mutable.LinkedHashSet

        val uniques = (LinkedHashSet() ++ historyLinesRead).toSeq
        val historyLines =
          searchTerm match {
            case Some(term) => uniques.filter {
              case DabbleHistoryLine(deps, _, _) =>
                !deps.list.filter {
                  case ScalaVersionSupplied(org, name, _, _) =>
                    org.contains(term) || name.contains(term)
                  case ScalaVersionDerived(org, name, _, _) =>
                    org.contains(term) || name.contains(term)
                }.isEmpty
            }

            case None => uniques
          }

        if (historyLines.nonEmpty) {

          val header  = "Dabble History"
          println(header)
          println(List.fill(header.length)("-").mkString)
          println

          historyLines.zipWithIndex.foreach {
            case (line, i) => println(s"[${i+1}] ${printHistoryLine(line)}")
          }

          readInput(historyLines.length) match {
            case \/-(_) =>
            case -\/(n)=>
              val line = historyLines(n)
              println(s"${newline}Launching dabble with:${newline}${printHistoryLine(line)}${newline}")
              val DabbleHistoryLine(dependencies, resolvers, mpVersion) = line
              build(dependencies.list.toList, resolvers, mpVersion)
          }
        } else {
          println("You have not made history. ")
          println("Dabble writes out a history line when you successfully load a dependency and exit.")
        }

        exit(ExecutionResult(message = None, code = 0))
      case None =>
        exit(processingFailed)
    }
  }

  private def readInput(lines: Int): Int \/ String = {
    println
    println(s"Please select a number between 1 and ${lines} or type q to exit")
    val choice = readLine
    if (Try(choice.toInt).map(c => c <= lines).toOption.getOrElse(false))
      Math.max(0, choice.toInt - 1).left[String]
    else if (choice == "q") "q".right[Int]
    else readInput(lines)
  }
}
