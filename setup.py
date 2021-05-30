import setuptools

with open("README.md", "r") as fh:
    long_description = fh.read()

with open("requirements.txt", "r") as fh:
    requirements = fh.readline()

setuptools.setup(
    name='hadoop-protoseq',
    version='0.0.1',
    description='Python library for Hadoop Streaming with support of protobuf sequences',
    long_description=long_description,
    long_description_content_type='text/markdown',
    keywords='hadoop, streaming, protobuf',
    author='Bugaevskii Vladimir',
    author_email='bugaevsky@mail.ru',
    url='https://github.com/vbugaevskii/hadoop-streaming-protoseq',
    download_url='https://github.com/vbugaevskii/hadoop-streaming-protoseq/archive/master.tar.gz',
    license="MIT",
    packages=setuptools.find_packages(),
    install_requires=requirements,
    classifiers=[
        'Environment :: Console',
        'Intended Audience :: Developers',
        'License :: OSI Approved :: MIT License',
        'Natural Language :: English',
        'Operating System :: POSIX :: Linux',
        'Programming Language :: Python :: 3',
        'Programming Language :: Python :: 3.7',
    ],
    python_requires='>=3.7',
)
