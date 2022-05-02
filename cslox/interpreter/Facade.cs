using System;
using System.IO;


namespace Lox;

public class Facade {
    public void Run(string filename) {
        Console.WriteLine($"Lox program is loaded from file {filename}");
        string text = File.ReadAllText(filename);
        Console.WriteLine(text);
    }
}
