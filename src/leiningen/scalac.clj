(ns leiningen.scalac
  (:require [lancet.core :as lancet]
            [leiningen.core.eval :as eval]
            [leiningen.classpath :as classpath])
  (:import (org.apache.tools.ant.types Path)))

(defn task-props [project]
  (merge {:srcdir (:scala-source-path project)
          :destdir (:compile-path project)}
         (:scalac-options project)))

(defn scalac
  "Compile Scala source in :scala-source-path to :compile-path.

Set :scalac-options in project.clj to pass options to the Scala compiler.
See http://www.scala-lang.org/node/98 for details."
  [project]
  (eval/eval-in-project
   (update-in
    project [:dependencies]
    conj
    ['org.scala-lang/scala-compiler (or (:scala-version project) "2.9.2")]
    ['lancet "1.0.1"])
   `(do
      (require 'lancet.core)
      (.addTaskDefinition
       lancet.core/ant-project "scalac" scala.tools.ant.Scalac)
      (lancet.core/define-ant-task ~(symbol 'ant-scalac) ~(symbol 'scalac))
      (let [classpath# ~(classpath/get-classpath-string project)
            task# (doto (lancet.core/instantiate-task
                         lancet.core/ant-project "scalac"
                         ~(task-props project))
                    (.setClasspath (Path. lancet.core/ant-project classpath#)))]
        (lancet.core/mkdir {:dir ~(:compile-path project)})
        (.execute task#)))))
