const Discord = require("discord.js");

module.exports = {
    // Info
    name: 'specialquestion',
    description: 'Add a special question to the #help-desk embed that when used will apply a role to the user who used it.',
    aliases: ['squestion'],
    args: true,
    usage: '<question ||| @role>',
    cooldown: 5,
    // Basic checks
    guildOnly: true,
    chooseDesk: true,
    // Command Category
    helpdesk: true,
    async execute(data, member, message, args, index) {
        const role = message.mentions.roles.last();
        if(!role) {
            message.client.errorEmbed.setDescription('You need to also tag a role that will be assigned to users who use this question.\nExample: `hd?sQuestion Assign a role? ||| @AnyRole`.');
            return message.channel.send(message.client.errorEmbed);
        }
        let parameters = args.join(' ').split('|||')
        if(!parameters || !parameters[0] || !parameters[1]) {
            message.client.errorEmbed.setDescription('You need to provide a question and a role to the question too.\nFor example `hd?addQuestion Assign a role? ||| @AnyRole`');
            return message.channel.send(message.client.errorEmbed);
        }
        const question = parameters[0];
        if(!role.editable) {
            message.client.errorEmbed.setDescription('The role you tagged is at an higher position in the server roles hierarchy. Please move Help Desk role above the tagged role in the server roles settings.\nLearn more with [this article](https://support.discord.com/hc/en-us/articles/214836687-Gestione-dei-Ruoli-101).');
            return message.channel.send(message.client.errorEmbed);
        }
        if(!role.editable) {
            message.client.errorEmbed.setDescription('The role you tagged is at an higher position in the server roles hierarchy. Please move Help Desk role above the tagged role in the server roles settings.\nLearn more with [this article](https://support.discord.com/hc/en-us/articles/214836687-Gestione-dei-Ruoli-101).');
            return message.channel.send(message.client.errorEmbed);
        }
        if(question.length > 500) {
            message.client.errorEmbed.setDescription('The question can\'t be longer than 500 characters.');
            return message.channel.send(message.client.errorEmbed);
        }
        data.helpDesks[index].specialQuestion = question;
        data.helpDesks[index].specialRole = role.id;
        console.log(data.helpDesks[index]);
        await message.client.guildSchema.updateOne({guildID: message.guild.id}, {$set: { ['helpDesks.'+index]: data.helpDesks[index] }});
        message.client.replyEmbed.setDescription('Special Question added. Use `hd?update` to apply the changes.');
        await message.channel.send(message.client.replyEmbed);
    },
};