package com.example.utils

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths, StandardOpenOption}

import com.example.apps.Application.logger
import com.typesafe.scalalogging.LazyLogging
import org.joda.time.DateTime

import scala.concurrent.{Await, Future}
import scala.io.Source
import scala.util.{Failure, Success, Try}

object AppUtils extends LazyLogging {
//  private val jacksonFactory = JacksonFactory.getDefaultInstance
//  private val mapClass: Class[_ <: util.HashMap[String, String]] = new util.HashMap[String, String]().getClass
//  private val jasonMapper = new ObjectMapper()
  private val timeout = AppConfig.defaultFutureTimeout

  def timeValidationCheck: Boolean = {
    val currentHour = DateTime.now.getHourOfDay
    logger.info(s"time validation check. current hour: $currentHour")
    currentHour > 6
  }

  def deleteFile(deletePathString: String): Boolean = {
    logger.info(s"delete file. delete file path: $deletePathString")
    val deletePath = Paths.get(deletePathString)

    Files.deleteIfExists(deletePath)
  }

  def readAndWriteFile(readPathString: String, writePathString: String, append: Boolean)(fn: String => String): Boolean = {
    logger.info(s"read and write file. read file path: $readPathString, write file path: $writePathString")
    val readPath = Paths.get(readPathString)
    val writePath = Paths.get(writePathString)

    if (Files.notExists(readPath)) {
      logger.error("read file is not exists")
      return false
    }

    if (Files.notExists(writePath)) {
      Files.createDirectories(writePath.getParent)
    }

    val fileWriter = if (append) {
      Files.newBufferedWriter(writePath,
        StandardCharsets.UTF_8,
        StandardOpenOption.CREATE,
        StandardOpenOption.APPEND)
    } else {
      Files.newBufferedWriter(writePath,
        StandardCharsets.UTF_8,
        StandardOpenOption.CREATE)
    }

    Source.fromFile(readPathString).getLines().foreach { line =>
      Try {
        fileWriter.write(fn(line))
        fileWriter.newLine()
      } match {
        case Success(_) =>
          logger.debug(s"write record: $line")
        case Failure(e) =>
          logger.error(e.getMessage)
          logger.error(line)
      }
    }

    logger.debug("file flushing and close.")
    fileWriter.flush()
    fileWriter.close()

    true
  }

  def writeFileIfNotExist(savePathString: String, record: String): Unit = {
    if (Files.notExists(Paths.get(savePathString))) {
      this.writeFile(savePathString, record, append = false)
    }
  }

  def writeFile(savePathString: String, record: String, append: Boolean): Unit = {
    logger.info(s"write file. file path: $savePathString")
    val savePath = Paths.get(savePathString)

    if (Files.notExists(savePath)) {
      Files.createDirectories(savePath.getParent)
    }

    val fileWriter = if (append) {
      Files.newBufferedWriter(savePath,
        StandardCharsets.UTF_8,
        StandardOpenOption.CREATE,
        StandardOpenOption.APPEND)
    } else {
      Files.newBufferedWriter(savePath,
        StandardCharsets.UTF_8,
        StandardOpenOption.CREATE,
        StandardOpenOption.WRITE)
    }

    Try {
      fileWriter.write(record)
    } match {
      case Success(_) =>
        logger.debug(s"write record: $record")
      case Failure(e) =>
        logger.error(e.getMessage)
        logger.error(record)
    }

    logger.debug("file flushing and close.")
    fileWriter.flush()
    fileWriter.close()
  }

  def writeFile(savePathString: String, records: Vector[String], append: Boolean): Unit = {
    logger.info(s"write file. file path: $savePathString")
    val savePath = Paths.get(savePathString)

    if (Files.notExists(savePath)) {
      Files.createDirectories(savePath.getParent)
    }

    val fileWriter = if (append) {
      Files.newBufferedWriter(savePath,
        StandardCharsets.UTF_8,
        StandardOpenOption.CREATE,
        StandardOpenOption.APPEND)
    } else {
      Files.newBufferedWriter(savePath,
        StandardCharsets.UTF_8,
        StandardOpenOption.CREATE)
    }

    records.foreach { record =>
      Try {
        fileWriter.write(record)
        fileWriter.newLine()
      } match {
        case Success(_) =>
          logger.debug(s"write record: $record")
        case Failure(e) =>
          logger.error(e.getMessage)
          logger.error(record)
      }
    }

    logger.debug("file flushing and close.")
    fileWriter.flush()
    fileWriter.close()
  }

//  def readJsonFromFileToMap(readFilePathString: String): mutable.Map[String, String] = {
//    logger.debug(s"read json from file: $readFilePathString")
//
//    val readPath = Paths.get(readFilePathString)
//
//    if (Files.notExists(readPath)) {
//      logger.error(s"file is not exist: $readPath")
//      return mutable.Map.empty[String, String]
//    }
//
//    val fileReader = Files.newBufferedReader(readPath)
//
//    val resultMap = Try(jacksonFactory.createJsonParser(fileReader).parse(mapClass).asScala) match {
//      case Success(result) => result
//      case Failure(exception) =>
//        logger.error(exception.getMessage, exception)
//        mutable.Map.empty[String, String]
//    }
//
//    fileReader.close()
//    resultMap
//  }
//
//  def readJsonStringToMap(jsonString: String): mutable.Map[String, String] = {
//    logger.debug("json string to map.")
//
//    Try(jacksonFactory.createJsonParser(jsonString).parse(mapClass).asScala) match {
//      case Success(result) => result
//      case Failure(exception) =>
//        logger.error(exception.getMessage, exception)
//        mutable.Map.empty[String, String]
//    }
//  }
//
//  def mapToJsonString[K, V](map: Map[K, V]): String = {
//    jasonMapper.writeValueAsString(map.asJava)
//  }

  def awaitResultFuture[R](future: Future[R]): R = Await.result(future, timeout)
}