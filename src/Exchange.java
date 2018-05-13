import java.util.ArrayList;
import java.util.List;

public class Exchange {
    private List<Currency> currencies = new ArrayList<>();

    public void addCurrency(Currency currency) {
        if (currency == null) {
            throw new IllegalArgumentException();
        }
        currencies.add(currency);
    }

    public List<Currency> getCurrencies() {
        return currencies;
    }

    public void setCurrencies(List<Currency> currencies) {
        this.currencies = currencies;
    }

    @Override
    public String toString() {
        return "Exchange{" +
                "currencies=" + currencies +
                '}';
    }
}
