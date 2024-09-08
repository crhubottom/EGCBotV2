package com.egc.bot.commands;

import com.egc.bot.commands.interfaces.ICommand;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.Collection;
import java.util.Dictionary;
import java.util.Hashtable;

/**
 * This is a base command that extends the Discord JDA Commands class because i hate the default implementation
 */
public class Command extends Commands {
    public static Dictionary<String, ICommand> commands = new Hashtable<>();

    /**
     * Adds a command to the internal commands table
     * @param name name of command (/name)
     * @param description description of command
     * @param command ICommand class whose run() function will be used on command execution
     * @return Discord JDA SlashCommandData
     */
    public static SlashCommandData slash(String name, String description, ICommand command) {
        commands.put(name, command);
        return Commands.slash(name, description);
    }

    /**
     * Command option with no default choices
     * @param type type of option
     * @param name name of option
     * @param description description of option
     * @param isRequired is option required for command execution?
     * @param isAutoComplete should auto complete be enabled?
     * @return Discord JDA OptionData 
     */
    public static OptionData option(OptionType type, String name, String description, Boolean isRequired, Boolean isAutoComplete) {
        return option(type, name, description, isRequired, isAutoComplete, null);
    }

    /**
     * Command option with no default choices
     * @param type type of option
     * @param name name of option
     * @param description description of option
     * @param isRequired is option required for command execution?
     * @param isAutoComplete should auto complete be enabled?
     * @param choices list of strings for choices to show
     * @return Discord JDA OptionData 
     */
    public static OptionData option(OptionType type, String name, String description, Boolean isRequired, Boolean isAutoComplete, Collection<String> choices) {
        OptionData optionData = new OptionData(type, name, description, isRequired);
        for (String choice : choices) {
            optionData.addChoice(choice, choice);
        }
        return optionData;
    }
}
