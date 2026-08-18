package in.lekhai.core.util;

import in.lekhai.core.enums.Roles;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class PermissionBitCalculator {

    /**
     * Extracts all enabled bit positions from permission bit array
     */
    public Set<Integer> extractEnabledBitPositions(List<Long> permissionBits) {
        Set<Integer> enabledBitSet = new HashSet<>();
        for(int index = 0; index < permissionBits.size(); index++) {
            Long value = permissionBits.get(index);
            int basePosition = index * 64;
            for(int bit = 0; bit < 64; bit++) {
                if((value & (1L << bit)) != 0) {
                    enabledBitSet.add(basePosition + bit);
                }
            }
        }
        return enabledBitSet;
    }

    /**
     * Calculates final permissions by AND-ing multiple permission lists
     */
    public List<Long> calculateFinalPermissions(
            List<Long> categoryPermissions,
            List<Long> rolePermissions,
            List<Long> userPermissions,
            Roles role
    ) {
        int maxSize = Math.max(
                categoryPermissions != null ? categoryPermissions.size() : 0,
                Math.max(
                        rolePermissions != null ? rolePermissions.size() : 0,
                        userPermissions != null ? userPermissions.size() : 0
                )
        );

        List<Long> finalPermissionBits = new ArrayList<>(maxSize);
        for(int i = 0; i < maxSize; i++)  {
            finalPermissionBits.add(
                    CollectionUtils.getOrDefault(categoryPermissions, i, Long.MAX_VALUE) &
                            CollectionUtils.getOrDefault(rolePermissions, i,
                                    Roles.SHOP_OWNER.equals(role) || Roles.SUPER_ADMIN.equals(role) ? Long.MAX_VALUE : 0L) &
                            CollectionUtils.getOrDefault(userPermissions, i, Long.MAX_VALUE)
            );
        }
        return finalPermissionBits;
    }

    /**
     * Enables specific bits in permission array
     */
    public void enableBits(List<Long> permissions, Set<Integer> bitPositions) {
        for (Integer bitPosition : bitPositions) {
            int index = bitPosition / 64;
            int bit = bitPosition % 64;

            while (permissions.size() <= index) {
                permissions.add(0L);
            }

            Long currentValue = permissions.get(index);
            permissions.set(index, currentValue | (1L << bit));
        }
    }

    public boolean isBitEnabled(List<Long> permissions, int bitPosition) {
        int index = bitPosition / 64;
        int bit = bitPosition % 64;

        // If index doesn't exist → bit is definitely not set
        if (index >= permissions.size()) {
            return false;
        }
        long value = permissions.get(index);
        return (value & (1L << bit)) != 0;
    }
}