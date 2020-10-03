module.exports = {
    // Info
    name: 'delspecialquestion',
    description: 'Delete the special question of the #help-desk embed.',
    aliases: ['dsquestion'],
    cooldown: 5,
    // Basic checks
    guildOnly: true,
    chooseDesk: true,
    // Command Category
    helpdesk: true,
    async execute(data, member, message, args, index) {
        data.helpDesks[index].specialQuestion = undefined;
        data.helpDesks[index].specialRole = undefined;
        await message.client.guildSchema.updateOne({guildID: message.guild.id}, {$set: { ['helpDesks.'+index]: data.helpDesks[index] }});
        await message.client.caches.hdel('settings', message.guild.id);

        message.client.replyEmbed.setDescription('Special Question deleted. Use `hd?update` to apply the changes.');
        await message.channel.send(message.client.replyEmbed);
    },
};