const Discord = require("discord.js");

module.exports = {
    // Info
    name: 'addquestion',
    description: 'Add a question to the #help-desk embed',
    aliases: ['aquestion'],
    args: true,
    usage: '<question ||| answer>',
    cooldown: 5,
    // Basic checks
    guildOnly: true,
    // Command Category
    helpdesk: true,
    async execute(data, member, message, args, index) {
        if(data.helpDesks[index].embedProperties.fields.length >= 25) {
            message.client.errorEmbed.setDescription('You already have 25 questions and that\'s the limit for all Discord Embeds.\nYou can delete some questions with `hd?delQuestion`.');
            return message.channel.send(message.client.errorEmbed);
        }
        let parameters = args.join(' ').split('|||')
        if(!parameters || !parameters[0] || !parameters[1]) {
            message.client.errorEmbed.setDescription('You need to provide a question and an answer to the question too.\nFor example `hd?addQuestion Need support? ||| Use the Help Desk!`');
            return message.channel.send(message.client.errorEmbed);
        }
        const question = parameters[0];
        const answer = parameters[1];
        if(question.length > 500) {
            message.client.errorEmbed.setDescription('The question can\'t be longer than 500 characters.');
            return message.channel.send(message.client.errorEmbed);
        }
        if(answer.length > 500) {
            message.client.errorEmbed.setDescription('The answer can\'t be longer than 500 characters.');
            return message.channel.send(message.client.errorEmbed);
        }
        data.helpDesks[index].embedProperties.fields.push({name: '\u200b', value: question, inline: false});
        data.helpDesks[index].fieldsReplies.push(answer);
        await message.client.guildSchema.updateOne({guildID: message.guild.id}, {$set: { ['helpDesks.'+index]: data.helpDesks[index] }});
        message.client.replyEmbed.setDescription('Question added. Use `hd?update` to apply the changes.');
        await message.channel.send(message.client.replyEmbed);
    },
};