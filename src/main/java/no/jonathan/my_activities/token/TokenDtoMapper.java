package no.jonathan.my_activities.token;

import java.util.function.Function;

public class TokenDtoMapper implements Function<Token, TokenDto> {

    @Override
    public TokenDto apply(Token token) {
        return new TokenDto(
                token.getId(),
                token.getToken(),
                token.getCreatedAt(),
                token.getExpiresAt(),
                token.getValidatedAt(),
                token.getUser().getId()
        );
    }
}
