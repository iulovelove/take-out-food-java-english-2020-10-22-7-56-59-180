import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * This Java source file was generated by the Gradle 'init' task.
 */
public class App {
    private ItemRepository itemRepository;
    private SalesPromotionRepository salesPromotionRepository;

    public App(ItemRepository itemRepository, SalesPromotionRepository salesPromotionRepository) {
        this.itemRepository = itemRepository;
        this.salesPromotionRepository = salesPromotionRepository;
    }

    public String bestCharge(List<String> inputs) {
        StringBuilder sb = new StringBuilder();
        String title = "============= Order details =============\n";
        sb.append(title);
        //获取菜单
        List<Item> menu = itemRepository.findAll();
        //获取输入菜单菜品
        Map<String, Item> receiptItem = new HashMap<>();
        for (Item item : menu) {
            receiptItem.put(item.getId(), item);
        }
        //记录输入菜品，以及对应的购买数量
        Map<String, Integer> cookOrder = new HashMap<>();
        for (String input : inputs) {
            String id = input.split(" x ")[0];
            Integer count = input.split(" x ").length > 1 ? Integer.parseInt(input.split(" x ")[1]) : 1;
            cookOrder.put(id, count);
            if (receiptItem.containsKey(id)) {
                Item currentItem = receiptItem.get(id);
                String output = currentItem.getName() + " x " + cookOrder.get(id) + " = " + outPrice(currentItem.getPrice() * cookOrder.get(id)) + " yuan\n";
                sb.append(output);
            }
        }
        sb.append("-----------------------------------\n");

        //计算消费
        List<SalesPromotion> promotions = salesPromotionRepository.findAll();
        //没有优惠的消费
        double count = 0;
        for (Map.Entry<String, Integer> e : cookOrder.entrySet()) {
            count += receiptItem.get(e.getKey()).getPrice() * e.getValue();
        }
        //半价消费
        SalesPromotion halfPromote = null;
        for (SalesPromotion promotion : promotions) {
            if (promotion.getType().equals(SALE_PROMOTION_TYPE.HALF_PRICE_FOR_SPECIFIED_ITEMS)) {
                halfPromote = promotion;
                break;
            }
        }
        double discountTot = 0;
        String halfItemName = "";
        if (halfPromote != null) {
            List<String> halfPromoteItemsIds = halfPromote.getRelatedItems();
            halfItemName += "(";
            for (String itemId : halfPromoteItemsIds) {
                if (cookOrder.containsKey(itemId)) {
                    Item item = receiptItem.get(itemId);
                    double currCount = item.getPrice() * (int) cookOrder.get(itemId);
                    discountTot += currCount / 2;
                    halfItemName += item.getName();
                    if (!itemId.equals(halfPromoteItemsIds.get(halfPromoteItemsIds.size() - 1))) {
                        halfItemName += "，";
                    } else halfItemName += ")";
                }
            }
        }
        double total = count;
        double halfPromotePrice = count - discountTot;
        if (count >= 30 && count - 6 <= halfPromotePrice) {
            sb.append("Promotion used:\n");
            //满30减6更加优惠，如果一样的优惠力度选择的则是满30减6
            sb.append("满30减6 yuan");
            sb.append("，saving 6 yuan\n");
            sb.append("-----------------------------------\n");
            total -= 6;
        } else {
            if (count == halfPromotePrice) {
                //如果不使用优惠和半价价钱一样，则说明没有合适的优惠可以使用
            } else {
                //剩下情况都是半价策略更优惠
                sb.append("Promotion used:\n");
                sb.append("Half price for certain dishes ");
                sb.append(halfItemName);
                sb.append("，saving " + outPrice(discountTot) + " yuan\n");
                sb.append("-----------------------------------\n");
                total = halfPromotePrice;
            }
        }
        //给出总价钱
        sb.append("Total：" + outPrice(total) + " yuan\n");
        sb.append("===================================");
        return sb.toString();
    }

    public String outPrice(double price) {
        String outPrice = String.valueOf(price);
        outPrice = outPrice.replaceAll("0+?$", "");//去掉多余的0
        outPrice = outPrice.replaceAll("[.]$", "");//如最后一位是.则去掉
        return outPrice;
    }

    interface SALE_PROMOTION_TYPE {
        String BUY_30_SAVE_6_YUAN = "BUY_30_SAVE_6_YUAN";
        String HALF_PRICE_FOR_SPECIFIED_ITEMS = "50%_DISCOUNT_ON_SPECIFIED_ITEMS";
    }
}



