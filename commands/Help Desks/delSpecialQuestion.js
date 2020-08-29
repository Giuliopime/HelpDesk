const Discord = require("discord.js");

module.exports = {
    // Info
    name: 'delSpecialQuestion',
    description: 'Delete the special question of the #help-desk embed.',
    aliases: ['dsquestion'],
    args: false,
    cooldown: 5,
    // Basic checks
    guildOnly: true,
    // Command Category
    helpdesk: true,
    async execute(data, member, message, args, index) {
        data.helpDesks[index].specialQuestion = undefined;
        data.helpDesks[index].specialRole = undefined;
        await message.client.guildSchema.updateOne({guildID: message.guild.id}, {$set: { ['helpDesks.'+index]: data.helpDesks[index] }});
        message.client.replyEmbed.setDescription('Special Question deleted. Use `hd?update` to apply the changes.');
        await message.channel.send(message.client.replyEmbed);
    },
};