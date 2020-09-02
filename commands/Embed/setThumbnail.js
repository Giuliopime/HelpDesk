module.exports = {
    // Info
    name: 'setthumbnail',
    description: 'Set a thumbnail to for the #help-desk embed',
    aliases: ['sthumbnail'],
    args: /^((https?:\/\/)?((([a-z\d]([a-z\d-]*[a-z\d])*)\.)+[a-z]{2,}|((\d{1,3}\.){3}\d{1,3}))(\:\d+)?(\/[-a-z\d%_.~+]*)*(\?[;&a-z\d%_.~+=-]*)?(\#[-a-z\d_]*)?|{delete})$/i,
    usage: '<URL / {delete} to remove>',
    cooldown: 5,
    // Basic checks
    guildOnly: true,
    // Command Category
    embed: true,
    // Permissions needed
    perms: ['EMBED_LINKS'],
    async execute(data, member, message, args, index) {
        let url = args[0];
        if(args[0] === '{delete}') url = undefined;
        await message.client.guildSchema.updateOne({guildID: message.guild.id},  { $set:{ ['helpDesks.' + index + '.embedProperties.thumbnail.url']: url } });
        message.client.replyEmbed.setDescription('Thumbnail set.\nUse `hd?update` to apply the changes to the #help-desk embed.');
        await message.channel.send(message.client.replyEmbed);
    },
};