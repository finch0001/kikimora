package prediction

import java.io.{File => JFile}

import scala.reflect.io.Directory

/**
 * Created by erna on 10/27/15.
 */
object DeleteData extends App {

  def deleteHugeFiles(maxSize:Int)(dir:Directory):Unit = {
    for(f ← dir.deepFiles){
      if(f.length > maxSize) {
        println(s"Deleting ${f.toAbsolute.path}")
        f.delete()
      }
    }
  }

  val trainDir = new JFile("train")
  val evalDir = new JFile("test")

  //Delete dangerously large files & too small files
  def deleteFilesLargerThan0MB = deleteHugeFiles(0) _
  deleteFilesLargerThan0MB(new Directory(trainDir))
  deleteFilesLargerThan0MB(new Directory(evalDir))

}
