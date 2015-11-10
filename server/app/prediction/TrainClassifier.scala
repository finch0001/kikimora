package prediction

import java.io.{File => JFile}

import nak.NakContext._
import nak.data._
import nak.liblinear.LiblinearConfig

import scala.io.Source
import scala.reflect.io.Directory

/**
 * Created by erna on 10/12/15.
 */
object TrainClassifier extends App {

  def deleteHugeFiles(maxSize:Int, minSize:Int)(dir:Directory):Unit = {
    for(f ← dir.deepFiles){
      if(f.length > maxSize || f.length < minSize) {
        println(s"Deleting ${f.toAbsolute.path}")
        f.delete()
      }
    }
  }
  //val newsgroupsDir = new File(args(0))

  // We need this codec for reading in the 20 news groups files.
  implicit val isoCodec = scala.io.Codec("UTF8")

  // stopword set
  val stopwords = (Source.fromFile("stopwordsQUELLE.txt").getLines).toSet

  // Train
  print("Training... ")
  val trainDir = new JFile("train")
  val evalDir = new JFile("test")

  //Delete dangerously large files & too small files
  def deleteFilesLargerThan5MB = deleteHugeFiles(3 * 1024 * 1024, 700) _
  deleteFilesLargerThan5MB(new Directory(trainDir))
  deleteFilesLargerThan5MB(new Directory(evalDir))

  val trainingExamples = fromLabeledDirs(trainDir).toIndexedSeq
  val config = LiblinearConfig(cost=5.0,eps=0.01)
  val featurizer = new BowFeaturizer(stopwords)
  val classifier = trainClassifier(config, featurizer, trainingExamples)
  saveClassifier(classifier,"classifier")

  // Comment out the above line and uncomment the following if you want to try
  // the hashing trick.
  //val classifier = trainClassifierHashed(config, featurizer, trainingExamples, 50000)

  println("done.")

}
