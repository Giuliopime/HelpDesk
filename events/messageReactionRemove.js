/*
This code is used to remove the @News role in the Help Desk Support Server
You can safely delete this file

module.exports = async (client, reaction, user) =>  {
    // If the reaction is on the #welcome message
    if(reaction.message.id === '749384378177683506' && reaction.message.guild) {
        // Get the member who used the reaction
        const member = reaction.message.guild.members.resolve(user.id);
        // Add the "News" role to the member
        if(member && member.roles.cache.has('749384562211029012')) await member.roles.remove('749384562211029012');
    }
}
 */