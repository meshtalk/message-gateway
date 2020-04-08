package tech.lerk.meshtalk.entities;

public class AdminCommand {
    private String password;
    private Command command;
    private String argument;

    public AdminCommand() {
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Command getCommand() {
        return command;
    }

    public void setCommand(Command command) {
        this.command = command;
    }

    public String getArgument() {
        return argument;
    }

    public void setArgument(String argument) {
        this.argument = argument;
    }

    public enum Command {
        ADD_GATEWAY, WIPE, REMOVE_GATEWAY
    }
}
