(ns tiltontec.webmx.core
  (:require
    [goog.dom :as dom]
    [tiltontec.model.core :as md]
    [tiltontec.webmx.html :refer [tag-dom-create *tag-trace*]]
    ;;[todomx.todomvc :as tmx]
    ;;[tiltontec.webmx.example.testing :as test]
    [tiltontec.webmx.example.todomvc :as todo]
    ;;[tiltontec.webmx.example.gentle-intro :as gi]
    ;;[tiltontec.webmx.example.ticktock :as tt]
    ;;git commit [tiltontec.webmx.example.startwatch :as sw]

    [taoensso.tufte :as tufte :refer (defnp p profiled profile)]
    [cljs-time.coerce :refer [from-long to-string] :as tmc])
  (:import [goog.date UtcDateTime]))

(enable-console-print!)

(tufte/add-basic-println-handler! {})

(let [root (dom/getElement "tagroot")

      ;; switch next to, eg, (gi/matrix-build!) to explore the gentle intro
      app-matrix (todo/matrix-build!)

      app-dom (binding [*tag-trace* nil]                ;; <-- set to nil if console too noisy
                (tag-dom-create
                  (md/md-get app-matrix :mx-dom)))

      n (.getTime (js/Date.))
      ;; tom (some-> n UtcDateTime.fromTimestamp)
      ;;   (some-> millis UtcDateTime.fromTimestamp))

      tom2  (tmc/from-long n)]

  (prn :now!!!! n :tom2 tom2 (tmc/to-string tom2))

  (prn :app-dom!!!! (str app-dom))

  (set! (.-innerHTML root) nil)
  (dom/appendChild root app-dom)
  (when-let [route-starter (md/md-get app-matrix :router-starter)]
    (route-starter)))
