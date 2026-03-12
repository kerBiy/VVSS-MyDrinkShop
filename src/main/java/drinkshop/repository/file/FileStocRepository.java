package drinkshop.repository.file;

import drinkshop.domain.Stoc;

public class FileStocRepository
        extends FileAbstractRepository<Integer, Stoc> {

    public FileStocRepository(String fileName) {
        super(fileName);
        loadFromFile();
    }

    @Override
    protected Integer getId(Stoc entity) {
        return entity.getId();
    }

    @Override
    protected Stoc extractEntity(String line) {
        String[] elems = line.split(",");

        int id = Integer.parseInt(elems[0].trim());
        String ingredient = elems[1].trim();
        double cantitate = Double.parseDouble(elems[2].trim());
        double stocMinim = Double.parseDouble(elems[3].trim());

        return new Stoc(id, ingredient, (int) cantitate, (int) stocMinim);
    }

    @Override
    protected String createEntityAsString(Stoc entity) {
        return entity.getId() + "," +
                entity.getIngredient() + "," +
                (int) entity.getCantitate() + "," +
                (int) entity.getStocMinim();
    }
}
