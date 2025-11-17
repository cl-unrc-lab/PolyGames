module SopaDeLetras where

import Data.List (sort, sortOn)

-- =========================================
-- Tipos de Datos empleados para modelar el problema
-- =========================================

type Posicion = (Int, Int)
type Letra = Char
type Casilla = (Posicion, Letra)
type Grilla = [[Casilla]] -- Grilla[filas][columnas]

-- =========================================
-- Construcción y visualización
-- =========================================

construirFilaGrilla :: Int -> String -> [Casilla]
construirFilaGrilla n fila = [ ((n, j), letra) | (j, letra) <- zip [0..] fila]

construirGrilla :: [String] -> Grilla
construirGrilla filas = [ construirFilaGrilla i fila | (i, fila) <- zip [0..] filas ]

-- Dibuja una fila con bordes
filaConBordes :: String -> [Casilla] -> String
filaConBordes borde fila =
  borde ++ "\n" ++
  concatMap (\(_, letra) -> "| " ++ [letra] ++ " ") fila ++ "|" 


-- Muestra la grilla con líneas y bordes
mostrarGrilla :: Grilla -> IO ()
mostrarGrilla grilla = do
  putStrLn "Grilla:"
  let nCols = length (head grilla)
      borde = "+" ++ concat (replicate nCols "---+")  -- línea superior/inferior
  mapM_ (putStrLn . filaConBordes borde) grilla
  putStrLn borde


-- =========================================
-- Transformaciones de grilla
-- =========================================

-- Trasponer la grilla: intercambia filas ↔ columnas pero conserva las posiciones originales.
-- Es decir, la fila k de la traspuesta contiene la k-ésima columna de la original,
-- manteniendo las Posiciones de las Casillas (no las reasigna).
trasponerGrilla :: Grilla -> Grilla
trasponerGrilla [] = []
trasponerGrilla ([]:_) = []
trasponerGrilla grilla =
  let nCols = length (head grilla)
  in [ [ row !! j | row <- grilla ] | j <- [0..(nCols-1)] ]


-- Espejar la grilla horizontalmente (revertir cada fila).
-- Conserva cada Casilla como estaba (posición, letra) pero invierte
-- el orden de las casillas en cada fila.
espejarGrilla :: Grilla -> Grilla
espejarGrilla grilla = map reverse grilla


-- Diagonales izq->der (top-left a bottom-right).
-- Agrupamos por (j - i). Conservamos las posiciones originales y
-- descartamos diagonales de longitud 1.
diagonalizarDesdeIzquierdaGrilla :: Grilla -> [[Casilla]]
diagonalizarDesdeIzquierdaGrilla grilla = filter (\f -> length f > 1) diagonales
  where
    -- lista plana de casillas
    flat :: [Casilla]
    flat = concat grilla

    -- clave = j - i
    claves :: [Int]
    claves = sort . eliminarRepetidos $ [ j - i | ((i,j), _) <- flat ]

    diagonales :: [[Casilla]]
    diagonales = [ sortOn (\((i,_),_) -> i) [ c | c@((i,j),_) <- flat, (j - i) == k ] | k <- claves ]


-- =========================================
-- Funciones auxiliares Subsegmentos
-- =========================================

eliminarRepetidos :: Eq a => [a] ->[a]
eliminarRepetidos [] = []
eliminarRepetidos (x:xs) = x : eliminarRepetidos (filter (/= x) xs)


subSegmentos :: Eq a => [a] -> [[a]]
subSegmentos xs = eliminarRepetidos $ [] : [ slice i j xs | i <- [0..(n-1)], j <- [i..(n-1)] ]
  where
    n = length xs
    slice i j ys = take (j - i + 1) (drop i ys)


-- =========================================
-- Calcular Líneas de Grilla
-- =========================================

filasGrilla :: Grilla -> [[Casilla]]
filasGrilla grilla = grilla

columnasGrilla :: Grilla -> [[Casilla]]
columnasGrilla grilla = trasponerGrilla grilla

-- Diagonales der->izq (top-right a bottom-left): agrupamos por (i + j)
diagonalesGrilla :: Grilla -> [[Casilla]]
diagonalesGrilla grilla = diagIzq ++ diagDer
  where
    diagIzq = diagonalizarDesdeIzquierdaGrilla grilla

    -- Para las diagonales der->izq usamos la clave (i + j)
    flat :: [Casilla]
    flat = concat grilla

    clavesDer :: [Int]
    clavesDer = sort . eliminarRepetidos $ [ i + j | ((i,j), _) <- flat ]

    diagDer :: [[Casilla]]
    diagDer = filter (\f -> length f > 1) $
              [ sortOn (\((i,_),_) -> i) [ c | c@((i,j),_) <- flat, (i + j) == k ] | k <- clavesDer ]


-- Todas las líneas: filas, columnas, diagonales (izq->der y der->izq)
-- y las reversas de todas ellas.
todasLasLineas :: Grilla -> [[Casilla]]
todasLasLineas g = lineas ++ lineasReversas
  where
    lineas = (filasGrilla g) ++ (columnasGrilla g) ++ (diagonalesGrilla g)
    lineasReversas = map reverse lineas


-- =========================================
-- Búsqueda de palabras
-- =========================================

type Resultado = (String, Posicion, Posicion)

-- Buscar ocurrencias de una palabra en una línea dada.
-- Se devuelven (palabra, posInicio, posFin) por cada aparición (incluye solapadas).
buscarEnLinea :: String -> [Casilla] -> [Resultado]
buscarEnLinea palabra linea
  | null palabra = []
  | otherwise = [ (palabra, fst (linea !! i), fst (linea !! (i + m - 1)))
                | i <- [0 .. length linea - m], matchesAt i ]
  where
    m = length palabra
    letrasLinea = map snd linea
    -- comprobar si la subcadena a partir de i coincide con la palabra
    matchesAt i = take m (drop i letrasLinea) == palabra

-- Buscar palabra en todas las líneas de la grilla
buscarPalabra :: Grilla -> String -> [Resultado]
buscarPalabra grilla palabra = concatMap (buscarEnLinea palabra) (todasLasLineas grilla)


-- Resolver la sopa de letras: buscar todas las palabras y eliminar duplicados en el resultado
resolverSopaLetras :: Grilla -> [String] -> [Resultado]
resolverSopaLetras grilla palabras = eliminarRepetidos $ concatMap (buscarPalabra grilla) palabras

g :: Grilla
g  = [
        [((0,0),'C'),((0,1),'S'),((0,2),'I'),((0,3),'S')],
        [((1,0),'A'),((1,1),'R'),((1,2),'E'),((1,3),'A')],
        [((2,0),'M'),((2,1),'C'),((2,2),'X'),((2,3),'O')],
        [((3,0),'A'),((3,1),'M'),((3,2),'A'),((3,3),'M')]
      ]

palabras = ["MAMA","CAMA","SI","SECA","AMA","ERA"]
