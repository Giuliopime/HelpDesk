module.exports = {
    // Info
    name: 'editquestion',
    description: 'Edit a question of the #help-desk',
    aliases: ['eq', 'equestion'],
    args: /^(\d)$/,
    usage: '<question number>',
    cooldown: 5,
    // Basic checks
    guildOnly: true,
    chooseDesk: true,
    // Command Category
    helpdesk: true,
    async execute(data, member, message, args, index) {
        let questionIndex = args[0];
        questionIndex--;

        await message.channel.send(message.client.replyEmbed.setDescription("Send in this channel the new question in the following format: `question ||| answer`").setFooter("You have 2 minutes to send the new question"));
        message.client.replyEmbed.setFooter("");

        let editedQuestion = false;

        const filter = m => m.author.id === member.id;
        const collector = message.channel.createMessageCollector(filter, { time: 120000 });

        collector.on('collect', async m => {
            const msgContent = m.content;

            const regexp = /^(.+?)( *\|\|\| *)([\s\S]+)$/;
            const matched = msgContent.match(regexp);
            if(!matched)
                m.channel.send(message.client.replyEmbed.setDescription("The new question is formatted incorrectly, make sure it follows the following format: `question ||| answer`"));
            else {
                const parts = matched.slice(1);
                editedQuestion = true;

                data.helpDesks[index].embedProperties.fields[questionIndex] = parts[0];
                data.helpDesks[index].fieldsReplies[questionIndex] = parts[2];

                await message.client.guildSchema.updateOne({ guildID: message.guild.id }, { $set: { ['helpDesks.' + index]: data.helpDesks[index] } });
                await message.client.caches.hdel('settings', message.guild.id);

                collector.stop();
            }
        });

        collector.on('end', async c => {
            const reply = message.client.replyEmbed;
            if(editedQuestion)
                reply.setDescription('Question edited successfully!\nUse `hd?update` to update the Help Desk.');
            else
                reply.setDescription('Question not edited!');

            await message.channel.send(reply);
        });
    },
};
