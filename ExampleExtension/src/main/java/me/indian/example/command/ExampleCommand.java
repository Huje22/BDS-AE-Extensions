package me.indian.example.command;

import me.indian.bds.command.Command;

import java.util.Arrays;

public class ExampleCommand extends Command {


    public ExampleCommand() {
        super("example", "Przykładowa komenda");
    }

    @Override
    public boolean onExecute(final String[] args, final boolean isOp) {
        this.sendMessage("&aTo przykładowa komenda wywołana z argumentami:&b " + Arrays.toString(args));
        this.sendMessage("&aJej wykonowaca ma op? :&b " + isOp);


        return false;
    }
}
