package gregtech.api.items.metaitem;

public class StandardMetaItem extends MetaItem<MetaItem<?>.MetaValueItem> {

    public StandardMetaItem(short metaItemOffset) {
        super(metaItemOffset);
    }

    @Override
    protected MetaValueItem constructMetaValueItem(short metaValue, String unlocalizedName, String... nameParameters) {
        return new MetaValueItem(metaValue, unlocalizedName, nameParameters);
    }

}
