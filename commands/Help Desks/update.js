const Discord = require("discord.js");

module.exports = {
    // Info
    name: 'update',
    description: 'Update the #help-desk embed',
    aliases: ['hdupdate'],
    args: false,
    cooldown: 5,
    // Basic checks
    guildOnly: true,
    // Command Category
    helpdesk: true,
    async execute(data, member, message, args, index) {
        let helpDesk = data.helpDesks[index];
        const hdChannel = await message.guild.channels.resolve(helpDesk.channelID);
        if(!hdChannel) {
            message.client.errorEmbed.setDescription('I couldn\'t find the #help-desk channel, if it has been deleted use the `hd?fix` command');
            return message.channel.send(message.client.errorEmbed);
        }
        const hdMessage = await hdChannel.messages.fetch(helpDesk.messageID);
        if(!hdMessage) {
            message.client.errorEmbed.setDescription('I couldn\'t find the #help-desk message embed, if it has been deleted use the `hd?fix` command');
            return message.channel.send(message.client.errorEmbed);
        }
        const embedProperties = helpDesk.embedProperties;
        let newEmbed = new Discord.MessageEmbed();
        if(embedProperties.title) newEmbed.setTitle(embedProperties.title)
        if(embedProperties.url) newEmbed.setURL(embedProperties.url)
        if(embedProperties.description) newEmbed.setDescription(embedProperties.description)
        if(embedProperties.thumbnail.url) newEmbed.setThumbnail(embedProperties.thumbnail.url)
        if(embedProperties.author.name) newEmbed.setAuthor(embedProperties.author.name, embedProperties.author.icon_url, embedProperties.author.url)
        if(embedProperties.color) newEmbed.setColor(embedProperties.color)
        if(embedProperties.footer) newEmbed.setFooter(embedProperties.footer)
        if(embedProperties.image.url) newEmbed.setImage(embedProperties.image.url)
        if(embedProperties.timestamp) newEmbed.setTimestamp(embedProperties.timestamp)
        hdMessage.edit(newEmbed)
            .then(msg=>{
                message.client.replyEmbed.setDescription(`Embed correctly updated: [embed](${msg.url})`);
                message.channel.send(message.client.replyEmbed);
            })
    },
};