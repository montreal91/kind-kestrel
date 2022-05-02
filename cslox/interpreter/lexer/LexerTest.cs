using Xunit;

namespace Lox;

public class LexerTest {
    [Fact]
    public void SmokeTest() {
        Assert.True(new Lexer().TwoByTwo() == 4);
    }
}
