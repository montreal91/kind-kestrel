using Xunit;

namespace Lox;

public class TokenTest {
    [Fact]
    public void ValidEquals() {
        Token t1 = new Token(TokenType.AND, 1.0, 1, 1);

        Assert.False(t1 == null);
        Assert.False(null == t1);

        Token equal = new Token(TokenType.AND, 1.0, 1, 1);
        Assert.True(t1 == equal);
    }

    [Theory]
    [InlineData(1.0)]
    [InlineData(true)]
    [InlineData(false)]
    [InlineData(null)]
    [InlineData("value")]
    public void EqualValues(object? value) {
        Token t1 = new Token(TokenType.CLASS, clone(value), 1, 1);
        Token t2 = new Token(TokenType.CLASS, clone(value), 1, 1);
        Assert.True(t1 == t2);
    }

    private object? clone(object? d) {
        if (d is double) {
            return (double) d;
        }

        if (d is string) {
            # pragma warning disable CS0618
            // This is done only for test purposes
            return string.Copy((string) d);
        }
        return d;
    }
}
