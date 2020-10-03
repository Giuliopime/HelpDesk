module.exports = {
    // Info
    name: 'specialquestion',
    description: 'Add a special question to the #help-desk embed that when used will apply a role to the user who used it.',
    aliases: ['squestion'],
    args: /^(.+?)( *\|\|\| *)(<@&\d{18}>|\d{18})$/,
    usage: '<question> ||| <@role / roleID>',
    cooldown: 5,
    // Basic checks
    guildOnly: true,
    chooseDesk: true,
    // Command Category
    helpdesk: true,
    async execute(data, member, message, args, index) {
        let roleID = args[2].replace('<@&', '').replace('>', '');
        const role = message.guild.roles.resolve(roleID);
        if(!role) {
            message.client.errorEmbed.setDescription('I couldn\'t find a role with that ID, make sure you tag a role or use a correct role ID.');
            return message.channel.send(message.client.errorEmbed);
        }
        if(!role.editable) {
            message.client.errorEmbed.setDescription('The role you tagged is at an higher position in the server roles hierarchy. Please move Help Desk role above the tagged role in the server roles settings.\nLearn more with [this article](https://support.discord.com/hc/en-us/articles/214836687-Gestione-dei-Ruoli-101).');
            return message.channel.send(message.client.errorEmbed);
        }
        if(args[0].length > 1024) {
            message.client.errorEmbed.setDescription('The question can\'t be longer than 1024 characters.');
            return message.channel.send(message.client.errorEmbed);
        }
        data.helpDesks[index].specialQuestion = args[0];
        data.helpDesks[index].specialRole = roleID;
        await message.client.guildSchema.updateOne({guildID: message.guild.id}, {$set: { ['helpDesks.'+index]: data.helpDesks[index] }});
        await message.client.caches.hdel('settings', message.guild.id);

        message.client.replyEmbed.setDescription('Special Question added. Use `hd?update` to apply the changes.');
        await message.channel.send(message.client.replyEmbed);
    },
};