(ns tiltontec.tag.core
  (:require
    [goog.dom :as dom]
    [tiltontec.model.core :as md]
    [tiltontec.tag.html :refer [tag-dom-create *tag-trace*]]
    ;;[todomx.todomvc :as tmx]
    [tiltontec.tag.example.testing :as test]
    [tiltontec.tag.example.todomvc :as todo]
    ;;[tiltontec.tag.example.gentle-intro :as gi]
    ;;[tiltontec.tag.example.ticktock :as tt]
    ;;git commit [tiltontec.tag.example.startwatch :as sw]
    [ajax.core :refer [GET POST]]
    [taoensso.tufte :as tufte :refer (defnp p profiled profile)]))

(enable-console-print!)

(tufte/add-basic-println-handler! {})

(let [root (dom/getElement "tagroot")

      ;; switch next to, eg, (gi/matrix-build!) to explore the gentle intro
      app-matrix (todo/matrix-build!)

      app-dom (binding [*tag-trace* nil]                ;; <-- set to nil if console too noisy
                (tag-dom-create
                  (md/md-get app-matrix :mx-dom)))]

  (prn :app-dom!!!! (str app-dom))
  (set! (.-innerHTML root) nil)
  (dom/appendChild root app-dom)
  (when-let [route-starter (md/md-get app-matrix :router-starter)]
    (route-starter)))
