object BuildModules {

    object Common {
        const val AUTH = ":common:auth"
        const val CORE = ":common:core"
        const val DESIGN = ":common:design"
        const val DESIGN_COMPOSE = ":common:design-compose"
        const val LESSONS_DATA = ":common:lessons-data"
        const val MODELS = ":common:models"
        const val NETWORK = ":common:network"
        const val STORAGE = ":common:storage"
        const val TRANSLATIONS = ":common:translations"
    }

    object Features {
        const val ACCOUNT = ":features:account"
        const val APP_WIDGETS = ":features:app-widgets"
        const val BIBLE = ":features:bible"
        const val LESSONS = ":features:lessons"
        const val MEDIA = ":features:media"
        const val PDF = ":features:pdf"
        const val READER = ":features:reader"
        const val SETTINGS = ":features:settings"
    }

    object Libraries {
        const val TEST_UTILS = ":libraries:test_utils"
    }
}
