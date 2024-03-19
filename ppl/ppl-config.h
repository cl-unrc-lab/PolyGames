/* config.h.  Generated from config.h.in by configure.  */
/* config.h.in.  Generated from configure.ac by autoheader.  */


/* BEGIN ppl-config.h */

#ifndef PPL_ppl_config_h
#define PPL_ppl_config_h 1

/* Unique (nonzero) code for the IEEE 754 Half Precision
   floating point format.  */
#define PPL_FLOAT_IEEE754_HALF 1

/* Unique (nonzero) code for the IEEE 754 Single Precision
   floating point format.  */
#define PPL_FLOAT_IEEE754_SINGLE 2

/* Unique (nonzero) code for the IEEE 754 Double Precision
   floating point format.  */
#define PPL_FLOAT_IEEE754_DOUBLE 3

/* Unique (nonzero) code for the IEEE 754 Quad Precision
   floating point format.  */
#define PPL_FLOAT_IEEE754_QUAD 4

/* Unique (nonzero) code for the Intel Double-Extended
   floating point format.  */
#define PPL_FLOAT_INTEL_DOUBLE_EXTENDED 5


/* Define if building universal (internal helper macro) */
/* #undef AC_APPLE_UNIVERSAL_BUILD */

/* Defined if the C++compiler supports C++11 features. */
#define PPL_HAVE_CXX11 /**/

/* Define to 1 if you have the declaration of `ffs', and to 0 if you don't. */
#define PPL_HAVE_DECL_FFS 1

/* Define to 1 if you have the declaration of `fma', and to 0 if you don't. */
#define PPL_HAVE_DECL_FMA 0

/* Define to 1 if you have the declaration of `fmaf', and to 0 if you don't.
   */
#define PPL_HAVE_DECL_FMAF 1

/* Define to 1 if you have the declaration of `fmal', and to 0 if you don't.
   */
#define PPL_HAVE_DECL_FMAL 1

/* Define to 1 if you have the declaration of `getenv', and to 0 if you don't.
   */
#define PPL_HAVE_DECL_GETENV 1

/* Define to 1 if you have the declaration of `getrusage', and to 0 if you
   don't. */
#define PPL_HAVE_DECL_GETRUSAGE 1

/* Define to 1 if you have the declaration of `rintf', and to 0 if you don't.
   */
#define PPL_HAVE_DECL_RINTF 1

/* Define to 1 if you have the declaration of `rintl', and to 0 if you don't.
   */
#define PPL_HAVE_DECL_RINTL 1

/* Define to 1 if you have the declaration of `RLIMIT_AS', and to 0 if you
   don't. */
#define PPL_HAVE_DECL_RLIMIT_AS 1

/* Define to 1 if you have the declaration of `RLIMIT_DATA', and to 0 if you
   don't. */
#define PPL_HAVE_DECL_RLIMIT_DATA 1

/* Define to 1 if you have the declaration of `RLIMIT_RSS', and to 0 if you
   don't. */
#define PPL_HAVE_DECL_RLIMIT_RSS 1

/* Define to 1 if you have the declaration of `RLIMIT_VMEM', and to 0 if you
   don't. */
#define PPL_HAVE_DECL_RLIMIT_VMEM 0

/* Define to 1 if you have the declaration of `setitimer', and to 0 if you
   don't. */
#define PPL_HAVE_DECL_SETITIMER 1

/* Define to 1 if you have the declaration of `setrlimit', and to 0 if you
   don't. */
#define PPL_HAVE_DECL_SETRLIMIT 1

/* Define to 1 if you have the declaration of `sigaction', and to 0 if you
   don't. */
#define PPL_HAVE_DECL_SIGACTION 1

/* Define to 1 if you have the declaration of `strtod', and to 0 if you don't.
   */
#define PPL_HAVE_DECL_STRTOD 1

/* Define to 1 if you have the declaration of `strtof', and to 0 if you don't.
   */
#define PPL_HAVE_DECL_STRTOF 1

/* Define to 1 if you have the declaration of `strtold', and to 0 if you
   don't. */
#define PPL_HAVE_DECL_STRTOLD 1

/* Define to 1 if you have the declaration of `strtoll', and to 0 if you
   don't. */
#define PPL_HAVE_DECL_STRTOLL 1

/* Define to 1 if you have the declaration of `strtoull', and to 0 if you
   don't. */
#define PPL_HAVE_DECL_STRTOULL 1

/* Define to 1 if you have the <dlfcn.h> header file. */
#define PPL_HAVE_DLFCN_H 1

/* Define to 1 if you have the <fenv.h> header file. */
#define PPL_HAVE_FENV_H 1

/* Define to 1 if you have the <getopt.h> header file. */
#define PPL_HAVE_GETOPT_H 1

/* Define to 1 if you have the <glpk/glpk.h> header file. */
/* #undef PPL_HAVE_GLPK_GLPK_H */

/* Define to 1 if you have the <glpk.h> header file. */
/* #undef PPL_HAVE_GLPK_H */

/* Define to 1 if you have the <ieeefp.h> header file. */
/* #undef PPL_HAVE_IEEEFP_H */

/* Define to 1 if you have the <inttypes.h> header file. */
#define PPL_HAVE_INTTYPES_H 1

/* Define to 1 if the system has the type `int_fast16_t'. */
#define PPL_HAVE_INT_FAST16_T 1

/* Define to 1 if the system has the type `int_fast32_t'. */
#define PPL_HAVE_INT_FAST32_T 1

/* Define to 1 if the system has the type `int_fast64_t'. */
#define PPL_HAVE_INT_FAST64_T 1

/* Define to 1 if the system has the type `siginfo_t'. */
#define PPL_HAVE_SIGINFO_T 1

/* Define to 1 if you have the <signal.h> header file. */
#define PPL_HAVE_SIGNAL_H 1

/* Define to 1 if you have the <stdint.h> header file. */
#define PPL_HAVE_STDINT_H 1

/* Define to 1 if you have the <stdio.h> header file. */
#define HAVE_STDIO_H 1

/* Define to 1 if you have the <stdlib.h> header file. */
#define PPL_HAVE_STDLIB_H 1

/* Define to 1 if you have the <strings.h> header file. */
#define PPL_HAVE_STRINGS_H 1

/* Define to 1 if you have the <string.h> header file. */
#define PPL_HAVE_STRING_H 1

/* Define to 1 if you have the <sys/resource.h> header file. */
#define PPL_HAVE_SYS_RESOURCE_H 1

/* Define to 1 if you have the <sys/stat.h> header file. */
#define PPL_HAVE_SYS_STAT_H 1

/* Define to 1 if you have the <sys/time.h> header file. */
#define PPL_HAVE_SYS_TIME_H 1

/* Define to 1 if you have the <sys/types.h> header file. */
#define PPL_HAVE_SYS_TYPES_H 1

/* Define to 1 if the system has the type `timeval'. */
#define PPL_HAVE_TIMEVAL 1

/* Define to 1 if typeof works with your compiler. */
#define PPL_HAVE_TYPEOF 1

/* Define to 1 if the system has the type `uintptr_t'. */
#define PPL_HAVE_UINTPTR_T 1

/* Define to 1 if the system has the type `uint_fast16_t'. */
#define PPL_HAVE_UINT_FAST16_T 1

/* Define to 1 if the system has the type `uint_fast32_t'. */
#define PPL_HAVE_UINT_FAST32_T 1

/* Define to 1 if the system has the type `uint_fast64_t'. */
#define PPL_HAVE_UINT_FAST64_T 1

/* Define to 1 if you have the <unistd.h> header file. */
#define PPL_HAVE_UNISTD_H 1

/* Define to 1 if `_mp_alloc' is a member of `__mpz_struct'. */
/* #undef PPL_HAVE___MPZ_STRUCT__MP_ALLOC */

/* Define to 1 if `_mp_d' is a member of `__mpz_struct'. */
/* #undef PPL_HAVE___MPZ_STRUCT__MP_D */

/* Define to 1 if `_mp_size' is a member of `__mpz_struct'. */
/* #undef PPL_HAVE___MPZ_STRUCT__MP_SIZE */

/* Define to the sub-directory where libtool stores uninstalled libraries. */
#define LT_OBJDIR ".libs/"

/* Define to the address where bug reports for this package should be sent. */
#define PPL_PACKAGE_BUGREPORT "ppl-devel@cs.unipr.it"

/* Define to the full name of this package. */
#define PPL_PACKAGE_NAME "the Parma Polyhedra Library"

/* Define to the full name and version of this package. */
#define PPL_PACKAGE_STRING "the Parma Polyhedra Library 1.3pre1"

/* Define to the one symbol short name of this package. */
#define PPL_PACKAGE_TARNAME "ppl"

/* Define to the home page for this package. */
#define PACKAGE_URL ""

/* Define to the version of this package. */
#define PPL_PACKAGE_VERSION "1.3pre1"

/* ABI-breaking extra assertions are enabled when this is defined. */
/* #undef PPL_ABI_BREAKING_EXTRA_DEBUG */

/* Not zero if the FPU can be controlled. */
#define PPL_CAN_CONTROL_FPU 0

/* Defined if the integral type to be used for coefficients is a checked one.
   */
/* #undef PPL_CHECKED_INTEGERS */

/* The number of bits of coefficients; 0 if unbounded. */
#define PPL_COEFFICIENT_BITS 0

/* The integral type used to represent coefficients. */
#define PPL_COEFFICIENT_TYPE mpz_class

/* This contains the options with which `configure' was invoked. */
#define PPL_CONFIGURE_OPTIONS "'--enable-interfaces=java' '--with-java=/Library/Java/JavaVirtualMachines/temurin-20.jdk/Contents/Home/' '--with-gmp=/opt/homebrew' 'CXXFLAGS=-std=c++11' '--disable-documentation'"

/* The unique code of the binary format of C++ doubles, if supported;
   undefined otherwise. */
#define PPL_CXX_DOUBLE_BINARY_FORMAT PPL_FLOAT_IEEE754_DOUBLE

/* The binary format of C++ floats, if supported; undefined otherwise. */
#define PPL_CXX_FLOAT_BINARY_FORMAT PPL_FLOAT_IEEE754_SINGLE

/* The unique code of the binary format of C++ long doubles, if supported;
   undefined otherwise. */
#define PPL_CXX_LONG_DOUBLE_BINARY_FORMAT PPL_FLOAT_IEEE754_DOUBLE

/* Not zero if the the plain char type is signed. */
#define PPL_CXX_PLAIN_CHAR_IS_SIGNED 1

/* Not zero if the C++ compiler provides long double numbers that have bigger
   range or precision than double. */
#define PPL_CXX_PROVIDES_PROPER_LONG_DOUBLE 0

/* Not zero if the C++ compiler supports __attribute__ ((weak)). */
#define PPL_CXX_SUPPORTS_ATTRIBUTE_WEAK 1

/* Not zero if the the IEEE inexact flag is supported in C++. */
#define PPL_CXX_SUPPORTS_IEEE_INEXACT_FLAG 0

/* Not zero if it is possible to limit memory using setrlimit(). */
#define PPL_CXX_SUPPORTS_LIMITING_MEMORY 0

/* Not zero if the C++ compiler supports zero_length arrays. */
#define PPL_CXX_SUPPORTS_ZERO_LENGTH_ARRAYS 1

/* Defined if floating point arithmetic may use the 387 unit. */
#define PPL_FPMATH_MAY_USE_387 1

/* Defined if floating point arithmetic may use the SSE instruction set. */
#define PPL_FPMATH_MAY_USE_SSE 1

/* Defined if GLPK provides glp_term_hook(). */
/* #undef PPL_GLPK_HAS_GLP_TERM_HOOK */

/* Defined if GLPK provides glp_term_out(). */
/* #undef PPL_GLPK_HAS_GLP_TERM_OUT */

/* Defined if GLPK provides lib_set_print_hook(). */
/* #undef PPL_GLPK_HAS_LIB_SET_PRINT_HOOK */

/* Defined if GLPK provides _glp_lib_print_hook(). */
/* #undef PPL_GLPK_HAS__GLP_LIB_PRINT_HOOK */

/* Defined if the integral type to be used for coefficients is GMP's one. */
#define PPL_GMP_INTEGERS 1

/* Not zero if GMP has been compiled with support for exceptions. */
#define PPL_GMP_SUPPORTS_EXCEPTIONS 1

/* Defined if the integral type to be used for coefficients is a native one.
   */
/* #undef PPL_NATIVE_INTEGERS */

/* Assertions are disabled when this is defined. */
#define PPL_NDEBUG 1

/* Not zero if doubles are supported. */
#define PPL_SUPPORTED_DOUBLE 0

/* Not zero if floats are supported. */
#define PPL_SUPPORTED_FLOAT 0

/* Not zero if long doubles are supported. */
#define PPL_SUPPORTED_LONG_DOUBLE 0

/* The size of `char', as computed by sizeof. */
#define PPL_SIZEOF_CHAR 1

/* The size of `double', as computed by sizeof. */
#define PPL_SIZEOF_DOUBLE 8

/* The size of `float', as computed by sizeof. */
#define PPL_SIZEOF_FLOAT 4

/* The size of `fp', as computed by sizeof. */
#define PPL_SIZEOF_FP 8

/* The size of `int', as computed by sizeof. */
#define PPL_SIZEOF_INT 4

/* The size of `int*', as computed by sizeof. */
#define PPL_SIZEOF_INTP 8

/* The size of `long', as computed by sizeof. */
#define PPL_SIZEOF_LONG 8

/* The size of `long double', as computed by sizeof. */
#define PPL_SIZEOF_LONG_DOUBLE 8

/* The size of `long long', as computed by sizeof. */
#define PPL_SIZEOF_LONG_LONG 8

/* The size of `mp_limb_t', as computed by sizeof. */
#define PPL_SIZEOF_MP_LIMB_T 8

/* The size of `short', as computed by sizeof. */
#define PPL_SIZEOF_SHORT 2

/* The size of `size_t', as computed by sizeof. */
#define PPL_SIZEOF_SIZE_T 8

/* Define to 1 if all of the C90 standard headers exist (not just the ones
   required in a freestanding environment). This macro is provided for
   backward compatibility; new code need not use it. */
#define PPL_STDC_HEADERS 1

/* Define PPL_WORDS_BIGENDIAN to 1 if your processor stores words with the most
   significant byte first (like Motorola and SPARC, unlike Intel). */
#if defined AC_APPLE_UNIVERSAL_BUILD
# if defined __BIG_ENDIAN__
#  define PPL_WORDS_BIGENDIAN 1
# endif
#else
# ifndef PPL_WORDS_BIGENDIAN
/* #  undef PPL_WORDS_BIGENDIAN */
# endif
#endif

/* When defined and libstdc++ is used, it is used in debug mode. */
/* #undef _GLIBCXX_DEBUG */

/* When defined and libstdc++ is used, it is used in pedantic debug mode. */
/* #undef _GLIBCXX_DEBUG_PEDANTIC */

/* Define to empty if `const' does not conform to ANSI C. */
/* #undef const */

/* Define to `__inline__' or `__inline' if that's what the C compiler
   calls it, or to nothing if 'inline' is not supported under any name.  */
#ifndef __cplusplus
/* #undef inline */
#endif

/* Define to __typeof__ if your compiler spells it that way. */
/* #undef typeof */

/* Define to the type of an unsigned integer type wide enough to hold a
   pointer, if such a type exists, and if the system does not define it. */
/* #undef uintptr_t */


#if defined(PPL_NDEBUG) && !defined(NDEBUG)
# define NDEBUG PPL_NDEBUG
#endif

/* In order for the definition of `int64_t' to be seen by Comeau C/C++,
   we must make sure <stdint.h> is included before <sys/types.hh> is
   (even indirectly) included.  Moreover we need to define
   __STDC_LIMIT_MACROS before the first inclusion of <stdint.h>
   in order to have the macros defined also in C++.  */

#ifdef PPL_HAVE_STDINT_H
# ifndef __STDC_LIMIT_MACROS
#  define __STDC_LIMIT_MACROS 1
# endif
# include <stdint.h>
#endif

#ifdef PPL_HAVE_INTTYPES_H
# include <inttypes.h>
#endif

#define PPL_U(x) x

#endif /* !defined(PPL_ppl_config_h) */

/* END ppl-config.h */

