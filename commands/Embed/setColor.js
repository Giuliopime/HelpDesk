module.exports = {
    // Info
    name: 'setcolor',
    description: 'Set a color to for the #help-desk embed',
    aliases: ['scolor'],
    args: true,
    usage: '<hexedecimal color (#000000)> or <{delete}> for my role color',
    cooldown: 5,
    // Basic checks
    guildOnly: true,
    // Command Category
    embed: true,
    // Permissions needed
    perms: ['EMBED_LINKS'],
    async execute(data, member, message, args, index) {
        let text = args[0];
        if(args[0] === '{delete}') text = message.guild.me.displayHexColor;
        else if(!/^#([0-9A-F]{3}){1,2}$/i.test(args[0])) return message.channel.send(message.client.errorEmbed.setDescription('Invalid Hexedecimal color.\nExample of a valid color: `#000000`.'));

        await message.client.guildSchema.updateOne({guildID: message.guild.id},  { $set:{ ['helpDesks.' + index + '.embedProperties.color']: text } });
        message.client.replyEmbed.setDescription('Coolor set.\nUse `hd?update` to apply the changes to the #help-desk embed.');
        await message.channel.send(message.client.replyEmbed);
    },
};