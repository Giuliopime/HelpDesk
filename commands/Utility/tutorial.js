const Discord = require('discord.js');

module.exports = {
    // Info
    name: 'tutorial',
    description: 'Get started with Help Desk!',
    cooldown: 20,
    // Basic checks
    guildOnly: true,
    // Command Category
    utility: true,
    // Permissions needed
    perms: ['ADD_REACTIONS', 'MANAGE_MESSAGES', 'ATTACH_FILES', 'EMBED_LINKS'],
    async execute(data, member, message) {
        // Define the arrays of the commands separed by category
        const pages = [' ', 'Create an #help-desk', 'Personalize the Embed Message', 'Add Questions', 'Special Question', 'Advanced Settings Editing'];
        let page = 1;

        const tutorialEmbed = new Discord.MessageEmbed()
            .setColor(message.client.mainColor)
            .setTitle('Welcome to the Tutorial!')
            .setThumbnail(message.client.user.displayAvatarURL())
            .setDescription(`Let\'s get started!\nNavigate trough this tutorial sections to learn how to use <@${message.guild.me.id}>.`)
            .addFields(
                { name: 'Tutorial Sections', value: '**>** Introduction (this page)\n**>** Create an #help-desk\n**>** Personalize the Embed Message\n**>** Add questions\n**>** Special Question\n**>** Advanced Settings Editing'},
                { name: 'How to move trough this tutorial', value: 'Use reactions below to navigate trough the tutorial pages'},
            )
            .setFooter(`Page ${page} of ${pages.length}`)
        message.channel.send(tutorialEmbed).then(async msg => {
            let nextEmoji = '➡';
            let stopEmoji = '❌';
            let beforeEmoji = '◀️';
            await msg.react(beforeEmoji);
            await msg.react(stopEmoji);
            await msg.react(nextEmoji);
            const Filter = (reaction, user) => (reaction.emoji.name === beforeEmoji || reaction.emoji.name === nextEmoji || reaction.emoji.name === stopEmoji) && user.id === message.author.id && user.id !== message.client.user.id;
            const otherFilter = (reaction, user) => (reaction.emoji.name !== beforeEmoji && reaction.emoji.name !== nextEmoji && reaction.emoji.name !== stopEmoji) || (user.id !== message.author.id && user.id !== message.client.user.id);

            const update = msg.createReactionCollector(Filter, { time: 600000 });
            const other = msg.createReactionCollector(otherFilter, { time: 600000 });

            let time = Date.now();
            function sleep(ms) {
                return new Promise(resolve => setTimeout(resolve, ms));
            }

            other.on('collect', (r, u) => {
                r.users.remove(u);
            });

            update.on('collect', async (r, u) => {
                time = Date.now();
                if (r.emoji.name === stopEmoji) {
                    update.stop();
                }
                if (r.emoji.name === nextEmoji) {
                    if (page === pages.length) return r.users.remove(u);
                    page++;
                    await r.users.remove(u);
                    tutorialEmbed.setFooter(`Page ${page} of ${pages.length}`);
                }
                if (r.emoji.name === beforeEmoji) {
                    if (page === 1) return r.users.remove(u);
                    page--;
                    await r.users.remove(u);
                    tutorialEmbed.setFooter(`Page ${page} of ${pages.length}`);
                }
                if (page === 1) {
                    tutorialEmbed
                        .setTitle('Welcome to the Tutorial!')
                        .setThumbnail(message.client.user.displayAvatarURL())
                        .setDescription(`Let\'s get started!\nNavigate trough this tutorial sections to learn how to use <@${message.guild.me.id}>.`)
                    tutorialEmbed.fields = [
                        { name: 'Tutorial Sections', value: '**>** Introduction (this page)\n**>** Create an #help-desk\n**>** Personalize the Embed Message\n**>** Add questions\n**>** Special Question\n**>** Advanced Settings Editing'},
                        { name: 'How to move trough this tutorial', value: 'Use reactions below to navigate trough the tutorial pages'},
                    ]
                    tutorialEmbed.image = undefined;
                }
                if (page === 2) {
                    tutorialEmbed
                        .setTitle(pages[page-1])
                        .setDescription('**First thing first let\'s create a new #help-desk, simply use the command `hd?new`.**\n\n*This will generate an #help-desk channel as well as a template embed (which is a particular type of message) that you will be able to tweak in the next section.*\n\n**>** Make sure to not change the bot permissions in the channel settings, this could make the bot not function correctly.\n\nNow you can move on to the next section.')
                    tutorialEmbed.image = undefined;
                    tutorialEmbed.thumbnail = undefined;
                    tutorialEmbed.fields = [];
                }
                if (page === 3) {
                    tutorialEmbed
                        .setTitle(pages[page-1])
                        .setDescription('**Let\'s modify the help-desk embed now.**\n\n**>** You can find a list of commands to do that using `hd?help embed`.\n**>** You can see how embeds are structured in the image below.\n\n*(Quick note: The thumbnail of the embed is the image in the upper right corner)*')
                        .setImage('https://i.ibb.co/5kKpQrs/embed-Example.png')
                        .setThumbnail(message.client.user.displayAvatarURL());
                    tutorialEmbed.fields = [];
                }
                if (page === 4) {
                    tutorialEmbed
                        .setTitle(pages[page-1])
                        .setDescription('**Now that you know how to personalize the help-desk embed, you can start adding questions to it.**\n\nThe main two commands for that are:\n**>** `hd?addQuestion`\n**>** `hd?delQuestion`\n\nYou can use `hd?help addQuestion` to learn how to add a question.\n\nYou can move on to the next sections to learn about the `Special Question`.')
                    tutorialEmbed.image = undefined;
                    tutorialEmbed.thumbnail = undefined;
                    tutorialEmbed.fields = [];
                }
                if (page === 5) {
                    tutorialEmbed
                        .setTitle(pages[page-1])
                        .setDescription('**The *Special Question* is a cool kind of question: when the user queries it a role will be applied to the user.**\n\n**>** You can learn how to add a *Special Question* using `hd?help specialQuestion`\n**>** You will always be able to delete it with `hd?delSpecialQuestion`.\n\nIn the next sections you can find out how to export and load the help-desk settings.');
                    tutorialEmbed.image = undefined;
                    tutorialEmbed.thumbnail = undefined;
                    tutorialEmbed.fields = [];
                }
                if (page === 6) {
                    tutorialEmbed
                        .setTitle(pages[page-1])
                        .setDescription('**If you know how to use json files you can edit my settings in a much quicker way!**\n\n**>** Download the current settings of an #help-desk with `hd?save`.\nThis will give you an example of what the help-desk settings look like.\n\n**>** Once you tweaked the json file a bit, you can load those settings into an help desk with `hd?load`.\n\nYou can use tools like [jsoneditoronline](https://jsoneditoronline.org/) to modify json files.\n\n**Just like that you can export and import #help-desk templates!**\n\n*The tutorial has ended, hopefully it was helpful to you.\nHere are some other useful links:*\n[Support](https://discord.gg/4BTXnXu) | [Invite](https://discord.com/oauth2/authorize?client_id=739796627681837067&scope=bot&permissions=268954832)');
                    tutorialEmbed.image = undefined;
                    tutorialEmbed.thumbnail = undefined;
                    tutorialEmbed.fields = [];
                }
                await msg.edit(tutorialEmbed);
            });
            update.on('end', async () => {
                time = undefined;
                await msg.reactions.removeAll();
            });
            let condition = true;
            while(condition) {
                if(!time) condition = false;
                await sleep(30000);
                if(Date.now() - time >= 300000) {
                    condition = false;
                    update.stop();
                }
            }
        });
    },
};