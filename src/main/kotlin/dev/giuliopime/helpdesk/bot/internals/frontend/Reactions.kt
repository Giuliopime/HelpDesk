package dev.giuliopime.helpdesk.bot.internals.frontend

object Reactions {
    object Extended {
        const val online = "<:online:738146560533200927>"
        const val idle = "<:away:738146560596115499>"
        const val dnd = "<:red_dot:738146560814350477>"
        const val offline = "<:offline:738146560809893929>"

        const val link = "<:link:823877377850736690>"

        const val checkmark = "<:checkmark:825411356173402112>"
        const val error = "<:error:823998802713247804>"
        const val question = "<:editpencil:825410492277325834>"

        const val settings = "<:settings:825410493807853618>"

        const val manage = "<:manage:825410492565553153>"
        const val list = "<:list:825410492440772638>"
        const val create = "<:create:825410491861303327>"
        const val edit = "<:edit:825410492407218206>"
        const val trash = "<:trash:825410493740482600>"

        const val yay = "<:yay:754669918032756768>"
    }

    object Unicode {
        val online = Extended.online.substring(2, Extended.online.length - 1)
        val idle = Extended.idle.substring(2, Extended.idle.length - 1)
        val dnd = Extended.dnd.substring(2, Extended.dnd.length - 1)
        val offline = Extended.offline.substring(2, Extended.offline.length - 1)

        val link = Extended.link.substring(2, Extended.link.length - 1)

        val checkmark = Extended.checkmark.substring(2, Extended.checkmark.length - 1)
        val error = Extended.error.substring(2, Extended.error.length - 1)
        val question = Extended.question.substring(2, Extended.question.length - 1)

        val settings = Extended.settings.substring(2, Extended.settings.length - 1)

        val manage = Extended.manage.substring(2, Extended.manage.length - 1)
        val list = Extended.list.substring(2, Extended.list.length - 1)
        val create = Extended.create.substring(2, Extended.create.length - 1)
        val edit = Extended.edit.substring(2, Extended.edit.length - 1)
        val trash = Extended.trash.substring(2, Extended.trash.length - 1)
    }
}
