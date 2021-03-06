package dev.acdcjunior.iimmutable;

import dev.acdcjunior.iimmutable.fn.ICommand;
import dev.acdcjunior.iimmutable.fn.IConsumer;
import dev.acdcjunior.iimmutable.fn.IFunction;
import dev.acdcjunior.iimmutable.fn.ISupplier;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;


public class IOptionTest {

    public static final IOption<Stuff> aNone = IOption.none();

    private static class Stuff {
        private final char val;

        public Stuff(char val) {
            this.val = val;
        }

        @Override
        public String toString() {
            return "Stuff[" + val + "]";
        }
    }

    public static final Stuff SA = new Stuff('a');
    public static final IOption<Stuff> aSome = IOption.some(SA);
    public static final Stuff SB = new Stuff('b');
    public static final ISupplier<Stuff> SB_SUPPLIER = new ISupplier<Stuff>() {
        @Override
        public Stuff get() {
            return SB;
        }
    };

    @Test
    public void empty__is_an_alias_to_none() {
        assertThat(IOption.empty()).isSameAs(IOption.none());
    }

    @Test
    @SuppressWarnings({"ConstantConditions", "CatchMayIgnoreException"})
    public void some() {
        assertThat(aSome).isEqualTo(aSome);
        try {
            IOption.some(null);
            fail("Should have thrown exception");
        } catch (Exception e) {
            assertThat(e).hasMessage("Argument of IOption.Some cannot be null");
        }
    }

    @Test
    public void none() {
        assertThat(IOption.none()).isEqualTo(IOption.none());
    }

    @Test
    public void toString_test() {
        assertThat(aNone.toString()).isEqualTo("None");
        assertThat(aSome.toString()).isEqualTo("Some(Stuff[a])");
    }

    @Test
    public void equals__hashCode() {
        assertThat(aNone).isEqualTo(aNone);
        assertThat(aNone).isNotEqualTo(aSome);
        assertThat(aNone).isSameAs(IOption.<Stuff>none());
        assertThat(aNone.hashCode()).isEqualTo(IOption.<Stuff>none().hashCode());

        assertThat(aSome).isEqualTo(IOption.some(SA));
        assertThat(aSome).isNotEqualTo(IOption.some(SB));
        assertThat(aSome.hashCode()).isEqualTo(IOption.some(SA).hashCode());
    }

    @Test
    public void ofNullable() {
        assertThat(IOption.ofNullable(SA)).isEqualTo(aSome);
        assertThat(IOption.ofNullable(null)).isEqualTo(IOption.none());
    }

    @Test
    public void toList() {
        assertThat(aSome.toList()).containsExactly(SA);
        assertThat(IOption.none().toList()).isEmpty();
    }

    @Test
    public void orNull() {
        assertThat(aSome.orNull()).isSameAs(SA);
        assertThat(IOption.none().orNull()).isNull();
    }

    @Test
    public void getOrElse() {
        assertThat(aSome.getOrElse(SB)).isSameAs(SA);
        assertThat(IOption.none().getOrElse(SB)).isSameAs(SB);
    }

    @Test
    public void getOrElse_fn() {
        assertThat(aSome.getOrElse(SB_SUPPLIER)).isSameAs(SA);
        assertThat(aNone.getOrElse(SB_SUPPLIER)).isSameAs(SB);
    }

    @Test
    @SuppressWarnings("CatchMayIgnoreException")
    public void get() {
        assertThat(aSome.get()).isSameAs(SA);
        try {
            IOption.none().get();
            fail("Should have thrown exception");
        } catch (Exception e) {
            assertThat(e).hasMessage("IOption.None has no value. If null would be an acceptable value in this case, use .orNull() instead of .get()");
        }
    }

    @Test
    public void isEmpty() {
        assertThat(aSome.isEmpty()).isFalse();
        assertThat(IOption.none().isEmpty()).isTrue();
    }

    @Test
    public void isDefined() {
        assertThat(aSome.isDefined()).isTrue();
        assertThat(IOption.none().isDefined()).isFalse();
    }

    @Test
    public void ifEmpty() {
        final List<Stuff> ls = new ArrayList<Stuff>();
        assertThat(aSome.ifEmpty(new ICommand() {
            @Override
            public void run() {
                ls.add(SA);
            }
        })).isEqualTo(aSome);
        assertThat(aNone.ifEmpty(new ICommand() {
            @Override
            public void run() {
                ls.add(SB);
            }
        })).isEqualTo(aNone);

        assertThat(ls).containsExactly(SB);
    }

    @Test
    public void ifDefined() {
        final List<Stuff> ls = new ArrayList<Stuff>();
        assertThat(aSome.ifDefined(new IConsumer<Stuff>() {
            @Override
            public void accept(Stuff stuff) {
                ls.add(stuff);
            }
        })).isEqualTo(aSome);
        assertThat(aNone.ifDefined(new IConsumer<Stuff>() {
            @Override
            public void accept(Stuff stuff) {
                ls.add(stuff);
            }
        })).isEqualTo(aNone);

        assertThat(ls).containsExactly(SA);
    }

    @Test
    public void orElse() {
        assertThat(aSome.orElse(SB)).isEqualTo(aSome);
        assertThat(aNone.orElse(SB)).isEqualTo(IOption.some(SB));
    }

    @Test
    public void orElse_supply() {
        assertThat(aSome.orElse(new ISupplier<Stuff>() {
            @Override
            public Stuff get() {
                return SB;
            }
        })).isEqualTo(aSome);
        assertThat(aNone.orElse(new ISupplier<Stuff>() {
            @Override
            public Stuff get() {
                return SB;
            }
        })).isEqualTo(IOption.some(SB));
    }

    @Test
    public void orElseFlat() {
        assertThat(aSome.orElseFlat(IOption.some(SB))).isEqualTo(aSome);
        assertThat(aNone.orElseFlat(IOption.some(SB))).isEqualTo(IOption.some(SB));
    }

    @Test
    public void orElseFlat_supply() {
        assertThat(aSome.orElseFlat(new ISupplier<IOption<Stuff>>() {
            @Override
            public IOption<Stuff> get() {
                return IOption.some(SB);
            }
        })).isEqualTo(aSome);
        assertThat(aNone.orElseFlat(new ISupplier<IOption<Stuff>>() {
            @Override
            public IOption<Stuff> get() {
                return IOption.some(SB);
            }
        })).isEqualTo(IOption.some(SB));
    }

    @Test
    public void filter() {
        assertThat(aSome.filter(new IFunction<Stuff, Boolean>() {
            @Override
            public Boolean apply(Stuff input) {
                return true;
            }
        })).isEqualTo(aSome);
        assertThat(aSome.filter(new IFunction<Stuff, Boolean>() {
            @Override
            public Boolean apply(Stuff input) {
                return false;
            }
        })).isEqualTo(IOption.none());
        assertThat(aNone.filter(new IFunction<Stuff, Boolean>() {
            @Override
            public Boolean apply(Stuff input) {
                return null;
            }
        })).isEqualTo(IOption.none());
    }

    @Test
    public void map() {
        assertThat(aSome.map(new IFunction<Stuff, Stuff>() {
            @Override
            public Stuff apply(Stuff input) {
                return SB;
            }
        })).isEqualTo(IOption.some(SB));
        assertThat(aSome.map(new IFunction<Stuff, Stuff>() {
            @Override
            public Stuff apply(Stuff input) {
                return null;
            }
        })).isEqualTo(IOption.none());
        assertThat(aNone.map(new IFunction<Stuff, Stuff>() {
            @Override
            public Stuff apply(Stuff input) {
                return SA;
            }
        })).isEqualTo(IOption.none());
    }

    @Test
    public void flatMap() {
        assertThat(aSome.flatMap(new IFunction<Stuff, IOption<Stuff>>() {
            @Override
            public IOption<Stuff> apply(Stuff input) {
                return IOption.some(SB);
            }
        })).isEqualTo(IOption.some(SB));
        assertThat(aSome.flatMap(new IFunction<Stuff, IOption<Stuff>>() {
            @Override
            public IOption<Stuff> apply(Stuff input) {
                return null;
            }
        })).isEqualTo(IOption.none());
        assertThat(aNone.flatMap(new IFunction<Stuff, IOption<Stuff>>() {
            @Override
            public IOption<Stuff> apply(Stuff input) {
                return aSome;
            }
        })).isEqualTo(IOption.none());
    }

    @Test
    public void forEach() {
        final List<Stuff> ls = new ArrayList<Stuff>();
        aSome.forEach(new IConsumer<Stuff>() {
            @Override
            public void accept(Stuff stuff) {
                ls.add(stuff);
            }
        });
        aNone.forEach(new IConsumer<Stuff>() {
            @Override
            public void accept(Stuff stuff) {
                ls.add(SB);
            }
        });

        assertThat(ls).containsExactly(SA);
    }

    @Test
    public void iterator() {
        List<Stuff> ls = new ArrayList<Stuff>();
        for (Stuff stuff : aSome) {
            ls.add(stuff);
        }
        for (Stuff ignored : aNone) {
            ls.add(SB);
        }

        assertThat(ls).containsExactly(SA);
    }

    @Test
    public void toEither___none() {
        final String leftVal = "@@@";
        ISupplier<String> ifNone = new ISupplier<String>() {
            @Override
            public String get() {
                return leftVal;
            }
        };
        assertThat(aSome.toEither(ifNone)).isEqualTo(IEither.right(SA));
        assertThat(aNone.toEither(ifNone)).isEqualTo(IEither.left(leftVal));
    }

}
