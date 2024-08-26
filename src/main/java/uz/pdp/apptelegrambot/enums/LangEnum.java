package uz.pdp.apptelegrambot.enums;

public enum LangEnum {
    RU,
    UZ,
    ENG,
    ;

    @Override
    public String toString() {
        return name().charAt(0) + name().substring(1).toLowerCase();
    }
}
