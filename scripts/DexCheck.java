import java.io.File;
import com.android.tools.smali.dexlib2.DexFileFactory;
import com.android.tools.smali.dexlib2.iface.*;

public class DexCheck {
    static String operands(
        com.android.tools.smali.dexlib2.iface.instruction.Instruction i
    ) {
        var out = new StringBuilder();
        var names = new java.util.TreeSet<String>();

        for (var m : i.getClass().getMethods()) {
            String n = m.getName();
            if (m.getParameterCount() == 0 &&
                (n.startsWith("getRegister") ||
                 n.equals("getStartRegister") ||
                 n.equals("getWideLiteral") ||
                 n.equals("getNarrowLiteral") ||
                 n.equals("getCodeOffset") ||
                 n.equals("getReference") ||
                 n.equals("getReference2") ||
                 n.equals("getCodeUnits"))) {
                names.add(n);
            }
        }

        for (String n : names) {
            try {
                Object v = i.getClass().getMethod(n).invoke(i);
                out.append(" ").append(n).append("=").append(v);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
        return out.toString();
    }

    public static void main(String[] args) throws Exception {
        var dex = DexFileFactory.loadDexContainer(
            new File(args[0]), null);
        int found = 0;

        for (var entry : dex.getDexEntryNames()) {
            for (var cls : dex.getEntry(entry)
                    .getDexFile().getClasses()) {
                if (!cls.getType().equals(args[1]))
                    continue;

                found++;
                System.out.println("CLASS " + cls.getType());

                for (var field : cls.getFields()) {
                    System.out.println("FIELD "
                        + field.getName() + ":" + field.getType());
                }

                for (var method : cls.getMethods()) {
                    if (!method.getName().equals("<clinit>"))
                        continue;

                    var impl = method.getImplementation();
                    if (impl == null)
                        throw new IllegalStateException("No implementation");

                    System.out.println("REGISTERS "
                        + impl.getRegisterCount());

                    int index = 0;
                    for (var ins : impl.getInstructions()) {
                        System.out.println(index++ + " "
                            + ins.getOpcode() + operands(ins));
                    }
                }
            }
        }

        if (found != 1)
            throw new IllegalStateException("Unexpected class count: " + found);
    }
}
