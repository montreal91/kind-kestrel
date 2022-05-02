using System;

public class Program {
    public static void Main(string[] args) {
        if (args.Length == 1) {
            Lox.Facade interpreter = new Lox.Facade();
            interpreter.Run(args[0]);
            Environment.Exit(0);
        }

        Console.WriteLine("Please specify filename to execute.");
        Environment.Exit(64);
    }
}
