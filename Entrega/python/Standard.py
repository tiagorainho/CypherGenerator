

import sys
import numpy as np

class Matrix(object):
    def __init__(self, exp, matrix):
        self.exp = exp
        self.representation = 2**exp
        self.matrix = self.get_matrix_array(matrix)
        self.assimilate()

    def get_value(self, x, y):
        return Num(self.exp, int(self.matrix[int(Num.get_value(x)),int(Num.get_value(y))]))
    
    def to_string(self):
        return self.matrix.view()
    
    def to_string_hex(self):
        result = "["
        for y in range(self.get_height()):
            result += "["
            for x in range(self.get_width()):
                if x > 0:
                    result += "\t"
                result += str(hex(self.matrix[y,x]))
            result += "]"
            if y < self.get_height()-1:
                result += "\n"
        return result + "]"

    def get_matrix(self, x1, y1, x2, y2):
        if Num.get_value(x1) > Num.get_value(x2) and Num.get_value(y1) > Num.get_value(y2):
            x1, x2 = x2, x1
            y1, y2 = y2, y1
        result = Matrix.zeros(self.get_type(), (abs(Num.get_value(y2)-Num.get_value(y1)+1), abs(Num.get_value(x2)-Num.get_value(x1)+1)))
        counter = 0
        for y in range(Num.get_value(y1), Num.get_value(y2)+1):
            result[counter] = self.get_row(y).get_object()[Num.get_value(x1):Num.get_value(x2)+1]
            counter += 1
        return Matrix(self.exp, result)

    def get_matrix_array(self, matrix):
        return np.array(matrix) if matrix.__class__.__name__ == "ndarray" or matrix.__class__.__name__ == "list" else matrix.matrix if matrix.__class__.__name__ == "Matrix" else sys.exit("ERROR: object not supported to be inserted as a Matrix")

    @staticmethod
    def zeros(representation, y, x):
        return Matrix(8 if representation == "byte" else 64 if representation == "num" else 1 if representation == "bit" else None, np.zeros((y, x), dtype = int))

    def set_value(self, y, x, value):
        self.matrix[Num.get_value(y),Num.get_value(x)] = Num.get_value(value) % self.representation
        return self

    def get_col(self, index):
        line = self.matrix[:, Num.get_value(index)]
        result = Matrix.zeros(self.get_type(), self.get_height(),1)
        for i in range(self.get_height()):
            result.matrix[i,0] = line[i]
        return Matrix(self.exp, result.matrix)

    def get_row(self, index):
        return Matrix(self.exp, [self.matrix[Num.get_value(index)]])

    def get_object(self):
        return self.matrix

    def to_line(self):
        return self.reshape(1,self.get_size())

    def reshape(self, x, y = 1):
        self.matrix = self.matrix.reshape(Num.get_value(x), Num.get_value(y))
        return self

    def get_width(self):
        return self.matrix.shape[1]

    def get_height(self):
        return self.matrix.shape[0]

    def get_size(self):
        return self.get_width() * self.get_height()

    def multiply(self, matrix):
        result = self.matrix.dot(matrix.get_object())
        return Matrix(self.exp, result)

    def get_type(self):
        return "bit" if self.exp == 0 else "byte" if self.exp == 8 else "num" if self.exp == 64 else None

    def operation_xor(self, matrix):
        return Matrix(self.exp, np.bitwise_xor(self.matrix, matrix.matrix))

    def operation_or(self, matrix):
        result = Matrix.zeros(self.get_type(), self.get_height(), self.get_width())
        for y in range(self.get_width()):
            for x in range(self.get_height()):
                result[x,y] = self.matrix[x,y] | matrix.matrix[x,y]
        return Matrix(self.exp, result)

    def operation_and(self, matrix):
        result = Matrix.zeros(self.get_type(), self.get_height(), self.get_width())
        for y in range(self.get_width()):
            for x in range(self.get_height()):
                result[x,y] = self.matrix[x,y] & matrix.matrix[x,y]
        return Matrix(self.exp, result)

    def assimilate(self):
        for y in range(self.get_width()):
            for x in range(self.get_height()):
                if self.representation == 1:
                    self.matrix[x,y] = 1 if self.matrix[x,y] > 0 else  0
                else:
                    self.matrix[x,y] = int(self.matrix[x,y] % self.representation)

    @staticmethod
    def get_representation(value):
        return 8 if "0x" in str(Num.get_value(value)) else 64

    @staticmethod
    def rotate_array(array, num):
        result = np.zeros(len(array), dtype = int)
        for i in range(len(array)):
            result[(i + Num.get_value(num)) % len(result)] = array[i]
        return result

    def rotate_row(self, rowIndex, num):
        self.matrix[Num.get_value(rowIndex)] = Matrix.rotate_array(self.matrix[Num.get_value(rowIndex)], self.get_width() - Num.get_value(num))
        return self
    
    def rotate_col(self, colIndex, num):
        resultant_matrix = Matrix.rotate_array(self.get_col(Num.get_value(colIndex)).get_object(), Num.get_value(num))
        self.matrix[:,Num.get_value(colIndex)] = resultant_matrix
        return self

    def set_col(self, matrix, colIndex):
        self.matrix[:,Num.get_value(colIndex)] = matrix.matrix[:,0]
        return self

    def set_row(self, matrix, rowIndex):
        self.matrix[Num.get_value(rowIndex)] = matrix.matrix[0]
        return self
    
    def overwrite(self, matrix, x, y):
        for i in range(matrix.get_height()):
            for j in range(matrix.get_width()):
                self.matrix[i+Num.get_value(y),j+Num.get_value(x)] = Num.get_value(matrix.get_value(i,j))
        return self

    def sub_bytes(self, matrix):
        if self.exp != 8:
            Utils.costom_error("Matrix \"{}\" has a representation of {} and should be {}".format(Utils.get_variable_name(self), self.representation, 255))
        if matrix.get_width() < 16 or matrix.get_height() < 16:
            Utils.costom_error("Matrix \"{}\" needs to have at least {} rows and columns".format(Utils.get_variable_name(matrix), 16))
        for x in range(self.get_height()):
            for y in range(self.get_width()):
                self.matrix[x,y] = matrix.get_value(Num(self.exp, self.matrix[x,y] // (self.representation / (2**(self.exp/2)))), Num(self.exp, self.matrix[x,y] % (self.representation / (2**(self.exp/2))))).value % self.representation
        return self

    @staticmethod
    def convert_string_to_bytes(string):
        str_bytes = str.encode(string)
        return [str_bytes[i] for i in range(len(str_bytes))]
    
    @staticmethod
    def get_array_of_arrays(array, length):
        length = Num.get_value(length)
        result = Matrix.zeros("byte",((len(array) // length) + 1), length)
        for i in range(0,len(array)+1, length):
            result.matrix[int(i/length)] = Matrix.convert_to_matrix(array[i:i+length], 1, length).matrix
        return result.matrix

    @staticmethod
    def get_array_of_matrices(array, y, x):
        x = Num.get_value(x)
        y = Num.get_value(y)
        result = np.zeros(((len(array) // (x * y) + 1),y,x), dtype = int)
        step = x*y
        for i in range(0,len(array)+1, step):
            result[int(i/step)] = Matrix.convert_to_matrix(array[i:i+step], y, x).matrix
        return result

    @staticmethod
    def convert_to_matrix(array, y, x):
        x = Num.get_value(x)
        y = Num.get_value(y)
        result = Matrix.zeros("byte", y, x)
        for j in range(y):
            for i in range(x):
                result.matrix[j,i] = array[j*x + i] if len(array) > j*x + i else x*y - len(array)
        return result


    @staticmethod
    def get_rijndael_s_box():
        return Matrix(8,[
            [0x63, 0x7c, 0x77, 0x7b, 0xf2, 0x6b, 0x6f, 0xc5, 0x30, 0x01, 0x67, 0x2b, 0xfe, 0xd7, 0xab, 0x76],
            [0xca, 0x82, 0xc9, 0x7d, 0xfa, 0x59, 0x47, 0xf0, 0xad, 0xd4, 0xa2, 0xaf, 0x9c, 0xa4, 0x72, 0xc0],
            [0xb7, 0xfd, 0x93, 0x26, 0x36, 0x3f, 0xf7, 0xcc, 0x34, 0xa5, 0xe5, 0xf1, 0x71, 0xd8, 0x31, 0x15],
            [0x04, 0xc7, 0x23, 0xc3, 0x18, 0x96, 0x05, 0x9a, 0x07, 0x12, 0x80, 0xe2, 0xeb, 0x27, 0xb2, 0x75],
            [0x09, 0x83, 0x2c, 0x1a, 0x1b, 0x6e, 0x5a, 0xa0, 0x52, 0x3b, 0xd6, 0xb3, 0x29, 0xe3, 0x2f, 0x84],
            [0x53, 0xd1, 0x00, 0xed, 0x20, 0xfc, 0xb1, 0x5b, 0x6a, 0xcb, 0xbe, 0x39, 0x4a, 0x4c, 0x58, 0xcf],
            [0xd0, 0xef, 0xaa, 0xfb, 0x43, 0x4d, 0x33, 0x85, 0x45, 0xf9, 0x02, 0x7f, 0x50, 0x3c, 0x9f, 0xa8],
            [0x51, 0xa3, 0x40, 0x8f, 0x92, 0x9d, 0x38, 0xf5, 0xbc, 0xb6, 0xda, 0x21, 0x10, 0xff, 0xf3, 0xd2],
            [0xcd, 0x0c, 0x13, 0xec, 0x5f, 0x97, 0x44, 0x17, 0xc4, 0xa7, 0x7e, 0x3d, 0x64, 0x5d, 0x19, 0x73],
            [0x60, 0x81, 0x4f, 0xdc, 0x22, 0x2a, 0x90, 0x88, 0x46, 0xee, 0xb8, 0x14, 0xde, 0x5e, 0x0b, 0xdb],
            [0xe0, 0x32, 0x3a, 0x0a, 0x49, 0x06, 0x24, 0x5c, 0xc2, 0xd3, 0xac, 0x62, 0x91, 0x95, 0xe4, 0x79],
            [0xe7, 0xc8, 0x37, 0x6d, 0x8d, 0xd5, 0x4e, 0xa9, 0x6c, 0x56, 0xf4, 0xea, 0x65, 0x7a, 0xae, 0x08],
            [0xba, 0x78, 0x25, 0x2e, 0x1c, 0xa6, 0xb4, 0xc6, 0xe8, 0xdd, 0x74, 0x1f, 0x4b, 0xbd, 0x8b, 0x8a],
            [0x70, 0x3e, 0xb5, 0x66, 0x48, 0x03, 0xf6, 0x0e, 0x61, 0x35, 0x57, 0xb9, 0x86, 0xc1, 0x1d, 0x9e],
            [0xe1, 0xf8, 0x98, 0x11, 0x69, 0xd9, 0x8e, 0x94, 0x9b, 0x1e, 0x87, 0xe9, 0xce, 0x55, 0x28, 0xdf],
            [0x8c, 0xa1, 0x89, 0x0d, 0xbf, 0xe6, 0x42, 0x68, 0x41, 0x99, 0x2d, 0x0f, 0xb0, 0x54, 0xbb, 0x16]
        ])

    @staticmethod
    def get_rijndael_s_box_inverted():
        return Matrix(8,[
            [0x52, 0x09, 0x6a, 0xd5, 0x30, 0x36, 0xa5, 0x38, 0xbf, 0x40, 0xa3, 0x9e, 0x81, 0xf3, 0xd7, 0xfb],
            [0x7c, 0xe3, 0x39, 0x82, 0x9b, 0x2f, 0xff, 0x87, 0x34, 0x8e, 0x43, 0x44, 0xc4, 0xde, 0xe9, 0xcb],
            [0x54, 0x7b, 0x94, 0x32, 0xa6, 0xc2, 0x23, 0x3d, 0xee, 0x4c, 0x95, 0x0b, 0x42, 0xfa, 0xc3, 0x4e],
            [0x08, 0x2e, 0xa1, 0x66, 0x28, 0xd9, 0x24, 0xb2, 0x76, 0x5b, 0xa2, 0x49, 0x6d, 0x8b, 0xd1, 0x25],
            [0x72, 0xf8, 0xf6, 0x64, 0x86, 0x68, 0x98, 0x16, 0xd4, 0xa4, 0x5c, 0xcc, 0x5d, 0x65, 0xb6, 0x92],
            [0x6c, 0x70, 0x48, 0x50, 0xfd, 0xed, 0xb9, 0xda, 0x5e, 0x15, 0x46, 0x57, 0xa7, 0x8d, 0x9d, 0x84],
            [0x90, 0xd8, 0xab, 0x00, 0x8c, 0xbc, 0xd3, 0x0a, 0xf7, 0xe4, 0x58, 0x05, 0xb8, 0xb3, 0x45, 0x06],
            [0xd0, 0x2c, 0x1e, 0x8f, 0xca, 0x3f, 0x0f, 0x02, 0xc1, 0xaf, 0xbd, 0x03, 0x01, 0x13, 0x8a, 0x6b],
            [0x3a, 0x91, 0x11, 0x41, 0x4f, 0x67, 0xdc, 0xea, 0x97, 0xf2, 0xcf, 0xce, 0xf0, 0xb4, 0xe6, 0x73],
            [0x96, 0xac, 0x74, 0x22, 0xe7, 0xad, 0x35, 0x85, 0xe2, 0xf9, 0x37, 0xe8, 0x1c, 0x75, 0xdf, 0x6e],
            [0x47, 0xf1, 0x1a, 0x71, 0x1d, 0x29, 0xc5, 0x89, 0x6f, 0xb7, 0x62, 0x0e, 0xaa, 0x18, 0xbe, 0x1b],
            [0xfc, 0x56, 0x3e, 0x4b, 0xc6, 0xd2, 0x79, 0x20, 0x9a, 0xdb, 0xc0, 0xfe, 0x78, 0xcd, 0x5a, 0xf4],
            [0x1f, 0xdd, 0xa8, 0x33, 0x88, 0x07, 0xc7, 0x31, 0xb1, 0x12, 0x10, 0x59, 0x27, 0x80, 0xec, 0x5f],
            [0x60, 0x51, 0x7f, 0xa9, 0x19, 0xb5, 0x4a, 0x0d, 0x2d, 0xe5, 0x7a, 0x9f, 0x93, 0xc9, 0x9c, 0xef],
            [0xa0, 0xe0, 0x3b, 0x4d, 0xae, 0x2a, 0xf5, 0xb0, 0xc8, 0xeb, 0xbb, 0x3c, 0x83, 0x53, 0x99, 0x61],
            [0x17, 0x2b, 0x04, 0x7e, 0xba, 0x77, 0xd6, 0x26, 0xe1, 0x69, 0x14, 0x63, 0x55, 0x21, 0x0c, 0x7d]
        ])

    @staticmethod
    def get_L():
        return Matrix(8,[
            [0x00,0x00,0x19,0x01,0x32,0x02,0x1A,0xC6,0x4B,0xC7,0x1B,0x68,0x33,0xEE,0xDF,0x03],
            [0x64,0x04,0xE0,0x0E,0x34,0x8D,0x81,0xEF,0x4C,0x71,0x08,0xC8,0xF8,0x69,0x1C,0xC1],
            [0x7D,0xC2,0x1D,0xB5,0xF9,0xB9,0x27,0x6A,0x4D,0xE4,0xA6,0x72,0x9A,0xC9,0x09,0x78],
            [0x65,0x2F,0x8A,0x05,0x21,0x0F,0xE1,0x24,0x12,0xF0,0x82,0x45,0x35,0x93,0xDA,0x8E],
            [0x96,0x8F,0xDB,0xBD,0x36,0xD0,0xCE,0x94,0x13,0x5C,0xD2,0xF1,0x40,0x46,0x83,0x38],
            [0x66,0xDD,0xFD,0x30,0xBF,0x06,0x8B,0x62,0xB3,0x25,0xE2,0x98,0x22,0x88,0x91,0x10],
            [0x7E,0x6E,0x48,0xC3,0xA3,0xB6,0x1E,0x42,0x3A,0x6B,0x28,0x54,0xFA,0x85,0x3D,0xBA],
            [0x2B,0x79,0x0A,0x15,0x9B,0x9F,0x5E,0xCA,0x4E,0xD4,0xAC,0xE5,0xF3,0x73,0xA7,0x57],
            [0xAF,0x58,0xA8,0x50,0xF4,0xEA,0xD6,0x74,0x4F,0xAE,0xE9,0xD5,0xE7,0xE6,0xAD,0xE8],
            [0x2C,0xD7,0x75,0x7A,0xEB,0x16,0x0B,0xF5,0x59,0xCB,0x5F,0xB0,0x9C,0xA9,0x51,0xA0],
            [0x7F,0x0C,0xF6,0x6F,0x17,0xC4,0x49,0xEC,0xD8,0x43,0x1F,0x2D,0xA4,0x76,0x7B,0xB7],
            [0xCC,0xBB,0x3E,0x5A,0xFB,0x60,0xB1,0x86,0x3B,0x52,0xA1,0x6C,0xAA,0x55,0x29,0x9D],
            [0x97,0xB2,0x87,0x90,0x61,0xBE,0xDC,0xFC,0xBC,0x95,0xCF,0xCD,0x37,0x3F,0x5B,0xD1],
            [0x53,0x39,0x84,0x3C,0x41,0xA2,0x6D,0x47,0x14,0x2A,0x9E,0x5D,0x56,0xF2,0xD3,0xAB],
            [0x44,0x11,0x92,0xD9,0x23,0x20,0x2E,0x89,0xB4,0x7C,0xB8,0x26,0x77,0x99,0xE3,0xA5],
            [0x67,0x4A,0xED,0xDE,0xC5,0x31,0xFE,0x18,0x0D,0x63,0x8C,0x80,0xC0,0xF7,0x70,0x07]
        ]).to_line()

    @staticmethod
    def get_E():
        return Matrix(8,[
            [0x01,0x03,0x05,0x0F,0x11,0x33,0x55,0xFF,0x1A,0x2E,0x72,0x96,0xA1,0xF8,0x13,0x35],
            [0x5F,0xE1,0x38,0x48,0xD8,0x73,0x95,0xA4,0xF7,0x02,0x06,0x0A,0x1E,0x22,0x66,0xAA],
            [0xE5,0x34,0x5C,0xE4,0x37,0x59,0xEB,0x26,0x6A,0xBE,0xD9,0x70,0x90,0xAB,0xE6,0x31],
            [0x53,0xF5,0x04,0x0C,0x14,0x3C,0x44,0xCC,0x4F,0xD1,0x68,0xB8,0xD3,0x6E,0xB2,0xCD],
            [0x4C,0xD4,0x67,0xA9,0xE0,0x3B,0x4D,0xD7,0x62,0xA6,0xF1,0x08,0x18,0x28,0x78,0x88],
            [0x83,0x9E,0xB9,0xD0,0x6B,0xBD,0xDC,0x7F,0x81,0x98,0xB3,0xCE,0x49,0xDB,0x76,0x9A],
            [0xB5,0xC4,0x57,0xF9,0x10,0x30,0x50,0xF0,0x0B,0x1D,0x27,0x69,0xBB,0xD6,0x61,0xA3],
            [0xFE,0x19,0x2B,0x7D,0x87,0x92,0xAD,0xEC,0x2F,0x71,0x93,0xAE,0xE9,0x20,0x60,0xA0],
            [0xFB,0x16,0x3A,0x4E,0xD2,0x6D,0xB7,0xC2,0x5D,0xE7,0x32,0x56,0xFA,0x15,0x3F,0x41],
            [0xC3,0x5E,0xE2,0x3D,0x47,0xC9,0x40,0xC0,0x5B,0xED,0x2C,0x74,0x9C,0xBF,0xDA,0x75],
            [0x9F,0xBA,0xD5,0x64,0xAC,0xEF,0x2A,0x7E,0x82,0x9D,0xBC,0xDF,0x7A,0x8E,0x89,0x80],
            [0x9B,0xB6,0xC1,0x58,0xE8,0x23,0x65,0xAF,0xEA,0x25,0x6F,0xB1,0xC8,0x43,0xC5,0x54],
            [0xFC,0x1F,0x21,0x63,0xA5,0xF4,0x07,0x09,0x1B,0x2D,0x77,0x99,0xB0,0xCB,0x46,0xCA],
            [0x45,0xCF,0x4A,0xDE,0x79,0x8B,0x86,0x91,0xA8,0xE3,0x3E,0x42,0xC6,0x51,0xF3,0x0E],
            [0x12,0x36,0x5A,0xEE,0x29,0x7B,0x8D,0x8C,0x8F,0x8A,0x85,0x94,0xA7,0xF2,0x0D,0x17],
            [0x39,0x4B,0xDD,0x7C,0x84,0x97,0xA2,0xFD,0x1C,0x24,0x6C,0xB4,0xC7,0x52,0xF6,0x01]
        ]).to_line()

    @staticmethod
    def get_rcon():
        return Matrix(8, [
            [0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, 0x80, 0x1B, 0x36],
            [0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00],
            [0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00],
            [0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00]
        ])

# ------------------------------------------------------------------------------------------------------------

    def split(self, number):
        return np.array_split(self.matrix, Num.get_value(number))

    def concat(self, other_matrix):
        np.concatenate(self.matrix, other_matrix)
        return self
    
    def stack(self, other_matrix, axis=1):
        np.stack((self.matrix, other_matrix), Num.get_value(axis))
        return self

    def rotate(self):
        self.matrix = np.rot90(self.matrix, 3)
        return self

    def switch_rows(self, row1, row2):
        self.matrix[[Num.get_value(row1), Num.get_value(row2)]] = self.matrix[[Num.get_value(row2), Num.get_value(row1)]]
        return self

    def switch_columns(self, col1, col2):
        self.matrix[:,[Num.get_value(col1), Num.get_value(col2)]] = self.matrix[:,[Num.get_value(col2), Num.get_value(col1)]]
        return self

    def switch_axis(self):
        self.rotate()
        middle = int(self.get_width()/2)
        for i in range(middle):
            self.switch_columns(i, self.get_width()-i-1)
        return self

class Utils():
    @staticmethod
    def get_variable_name(variable):
        variables = dict(globals())
        for name in variables:
            if variables[name] is variable:
                var_name = name
                break
        return var_name
    
    @staticmethod
    def incompatible_type_error(var1, type1, type2):
        sys.exit("ERROR: Variable \"{}\" is a {} and should be a {}".format(Utils.get_variable_name(var1), type1, type2))

    @staticmethod
    def incompatible_representation_error(var1, var2):
        sys.exit("ERROR: Variable \"{}\" has a representation of {} and should be {}".format(Utils.get_variable_name(var2), var2.representation, var1.representation))
    
    @staticmethod
    def costom_error(message):
        sys.exit("ERROR: " + message)
        

class Num(object):
    def __init__(self, exp, value):
        self.exp = exp
        self.representation = 2**exp
        if self.representation == 1:
            self.value = 1 if value > 0 else 0
        else:
            self.value = value % self.representation

    def add(self, op):
        self.verify(op)
        return Num(self.exp, (self.value + Num.get_value(op)))
    
    def subtract(self, op):
        self.verify(op)
        return Num(self.exp, (self.value - Num.get_value(op)))

    def divide(self, op):
        self.verify(op)
        return Num(self.exp, (self.value // Num.get_value(op)))
    
    def rem(self, op):
        self.verify(op)
        return Num(self.exp, (self.value % Num.get_value(op)))

    def multiply(self, op):
        self.verify(op)
        return Num(self.exp, (self.value * Num.get_value(op)))
    
    def pow(self, op):
        self.verify(op)
        return Num(self.exp, (self.value ** Num.get_value(op)))

    def operation_xor(self, op):
        self.verify(op)
        return Num(self.exp, (self.value ^ Num.get_value(op)))

    def operation_or(self, op):
        self.verify(op)
        return Num(self.exp, (self.value | Num.get_value(op)))

    def operation_and(self, op):
        self.verify(op)
        return Num(self.exp, (self.value & Num.get_value(op)))

    def to_num(self):
        return Num(64, self.value)

    def to_byte(self):
        return Num(8, self.value)

    def to_bit(self):
        return Num(0, self.value)
    
    def verify(self, op):
        if op.__class__.__name__ != "int":
            if(self.representation != op.representation):
                if self.get_type() != None and self.get_type() != None:
                    Utils.incompatible_type_error(self, self.get_type(), op.get_type())
                else:
                    Utils.incompatible_representation_error(self, op)
    
    def get_type(self):
        return "Bit" if self.exp == 0 else "Byte" if self.exp == 8 else "Num" if self.exp == 64 else None

    @staticmethod
    def get_value(num):
        return num.value if num.__class__.__name__ == "Num" else num