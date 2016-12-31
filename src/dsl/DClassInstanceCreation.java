package dsl;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.IMethodBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DClassInstanceCreation extends DExpression {

    final String node = "DClassInstanceCreation";
    String constructor;

    private DClassInstanceCreation(String constructor) {
        this.constructor = constructor;
    }

    public static class Handle extends Handler {
        ClassInstanceCreation creation;

        public Handle(ClassInstanceCreation creation, Visitor visitor) {
            super(visitor);
            this.creation = creation;
        }

        @Override
        public DClassInstanceCreation handle() {
            String className = checkAndGetClassName();
            if (className != null)
                return new DClassInstanceCreation(className + "." + getSignature(creation.resolveConstructorBinding()));
            return null;
        }

        @Override
        public void updateSequences(List<Sequence> soFar) {
            String className = checkAndGetClassName();
            if (className != null)
                for (Sequence seq : soFar)
                    seq.addCall(className + "." + getSignature(creation.resolveConstructorBinding()));
        }

        private String getSignature(IMethodBinding constructor) {
            Stream<String> types = Arrays.stream(constructor.getParameterTypes()).map(t -> t.getQualifiedName());
            return constructor.getName() + "(" + String.join(",", types.collect(Collectors.toCollection(ArrayList::new))) + ")";
        }

        /* check if the class corresponding to this instance creation is in API_CLASSES, and return
         * the class name if so (return null if not).
         */
        private String checkAndGetClassName() {
            IMethodBinding binding = creation.resolveConstructorBinding();
            if (binding != null && binding.getDeclaringClass() != null) {
                String className = binding.getDeclaringClass().getQualifiedName();
                if (className.contains("<")) /* be agnostic to generic versions */
                    className = className.substring(0, className.indexOf("<"));
                if (visitor.options.API_CLASSES.contains(className))
                    return className;
            }
            return null;
        }
    }
}
